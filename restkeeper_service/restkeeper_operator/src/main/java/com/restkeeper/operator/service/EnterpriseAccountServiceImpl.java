package com.restkeeper.operator.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.operator.config.RabbitMQConfig;
import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.operator.mapper.EnterpriseAccountMapper;
import com.restkeeper.sms.SmsObject;
import com.restkeeper.utils.*;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 企业账号管理 服务实现类
 */
@Service(version = "1.0.0", protocol = "dubbo")
@RefreshScope
public class EnterpriseAccountServiceImpl extends ServiceImpl<EnterpriseAccountMapper, EnterpriseAccount> implements IEnterpriseAccountService {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${sms.operator.signName}")
    private String signName;

    @Value("${gateway.secret}")
    private String secret;

    @Value("${sms.operator.templateCode}")
    private String templateCode;

    /**
     * 支持按照企业名称模糊查询及分页支持
     *
     * @param pageNum
     * @param pageSize
     * @param enterpriseName
     * @return
     */
    @Override
    public IPage<EnterpriseAccount> queryPageByName(int pageNum, int pageSize, String enterpriseName) {


        IPage<EnterpriseAccount> page = new Page<>(pageNum, pageSize);
        QueryWrapper<EnterpriseAccount> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(enterpriseName)) {
            queryWrapper.like("enterprise_name", enterpriseName);
        }
        return this.page(page, queryWrapper);
    }

    /**
     * 新增帐号
     *
     * @param account
     * @return
     */
    @Override
    @Transactional
    public boolean add(EnterpriseAccount account) {
        boolean flag = true;
        try {
            // 账号，密码特殊处理
            String shopId = getShopId();
            account.setShopId(shopId);
            //密码随机6位
            String pwd = RandomStringUtils.randomNumeric(6);
            account.setPassword(Md5Crypt.md5Crypt(pwd.getBytes()));

            this.save(account);
        } catch (Exception ex) {
            flag = false;
            throw ex;
        }
        return flag;
    }

    /**
     * 店铺id产生规则
     *
     * @return
     */
    private String getShopId() {
        //随机8位
        String shopId = RandomStringUtils.randomNumeric(8);
        //店铺校验
        QueryWrapper<EnterpriseAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", shopId);
        EnterpriseAccount account = this.getOne(queryWrapper);
        if (account != null) {
            this.getShopId();
        }
        return shopId;
    }

    @Override
    @Transactional
    public boolean recovery(String id) {
        return this.getBaseMapper().recovery(id);
    }


    //重置密码
    @Override
    @Transactional
    public boolean resetPwd(String id, String password) {
        boolean flag = true;
        try {
            EnterpriseAccount account = this.getById(id);
            if (account == null) {
                return false;
            }
            String newPwd;
            //如果设置了要重置密码
            if (StringUtils.isNotEmpty(password)) {
                newPwd = password;
            } else {
                //如果没有设置要重置密码
                newPwd = RandomStringUtils.randomNumeric(6);
            }
            account.setPassword(Md5Crypt.md5Crypt(newPwd.getBytes()));
            this.updateById(account);

            sendMessage(account.getPhone(), account.getShopId(), newPwd);
        } catch (Exception ex) {
            flag = false;
            throw ex;
        }
        return flag;
    }


    //发送短信
    private void sendMessage(String phone, String shopId, String pwd) {
        SmsObject smsObject = new SmsObject();
        smsObject.setPhoneNumber(phone);
        smsObject.setSignName(signName);
        smsObject.setTemplateCode(templateCode);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("shopId", shopId);
        jsonObject.put("password", pwd);
        smsObject.setTemplateJsonParam(jsonObject.toJSONString());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACCOUNT_QUEUE, JSON.toJSONString(smsObject));
    }

    /**
     * 用户登录
     *
     * @param shopId
     * @param telphone
     * @param loginPass
     * @return
     */
    @Override
    public Result login(String shopId, String telphone, String loginPass) {

        Result result = new Result();

        //参数校验
        if (StringUtils.isEmpty(shopId)) {
            result.setStatus(ResultCode.error);
            result.setDesc("用户名为空");
            return result;
        }

        if (StringUtils.isEmpty(loginPass)) {
            result.setStatus(ResultCode.error);
            result.setDesc("密码不存在");
            return result;
        }

        //查询用户信息
        QueryWrapper<EnterpriseAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(EnterpriseAccount::getPhone, telphone);
        queryWrapper.lambda().eq(EnterpriseAccount::getShopId, shopId);
        //未禁用状态
        queryWrapper.lambda().notIn(EnterpriseAccount::getStatus, AccountStatus.Forbidden.getStatus());
        EnterpriseAccount enterpriseAccount = this.getOne(queryWrapper);
        if (enterpriseAccount == null) {
            result.setStatus(ResultCode.error);
            result.setDesc("账号不存在");
            return result;
        }

        //密码校验
        String salts = MD5CryptUtil.getSalts(enterpriseAccount.getPassword());
        if (!Md5Crypt.md5Crypt(loginPass.getBytes(), salts).equals(enterpriseAccount.getPassword())) {
            result.setStatus(ResultCode.error);
            result.setDesc("密码不正确");
            return result;
        }

        //生成令牌
        Map<String, Object> tokenInfo = Maps.newHashMap();
        tokenInfo.put("shopId", enterpriseAccount.getShopId());
        tokenInfo.put("loginName", enterpriseAccount.getEnterpriseName());
        tokenInfo.put("userType", SystemCode.USER_TYPE_SHOP); // 集团管理用户
        String token = null;
        try {
            token = JWTUtil.createJWTByObj(tokenInfo, secret);
        } catch (Exception e) {
            log.error("加密失败" + e.getMessage());
            result.setStatus(ResultCode.error);
            result.setDesc("加密失败");
            return result;
        }
        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setData(enterpriseAccount);
        result.setToken(token);

        return result;
    }
}