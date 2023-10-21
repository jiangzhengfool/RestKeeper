package com.restkeeper.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.operator.mapper.EnterpriseAccountMapper;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.transaction.annotation.Transactional;

/**
 * 企业账号管理 服务实现类
 */
@Service(version = "1.0.0", protocol = "dubbo")
@RefreshScope
public class EnterpriseAccountServiceImpl extends ServiceImpl<EnterpriseAccountMapper, EnterpriseAccount> implements IEnterpriseAccountService {


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
        } catch (Exception ex) {
            flag = false;
            throw ex;
        }
        return flag;
    }
}