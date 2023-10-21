package com.restkeeper.operator.controller;

import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.operator.service.IEnterpriseAccountService;
import com.restkeeper.response.vo.AddEnterpriseAccountVO;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.response.vo.UpdateEnterpriseAccountVO;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@Api(tags = {"企业帐号管理"})
@RestController
@RequestMapping("/enterprise")
public class EnterpriseAccountController {

    @Reference(version = "1.0.0", check = false)
    private IEnterpriseAccountService enterpriseAccountService;

    /**
     * 查询分页数据
     */
    @ApiOperation(value = "查询企业账号(支持分页)")
    @GetMapping(value = "/pageList/{page}/{pageSize}")
    public PageVO<EnterpriseAccount> findListByPage(@PathVariable("page") int page,
                                                    @PathVariable("pageSize") int pageSize,
                                                    @RequestParam(value = "enterpriseName", required = false) String name) {
        return new PageVO<EnterpriseAccount>(enterpriseAccountService.queryPageByName(page, pageSize, name));
    }

    /**
     * 新增账号
     */
    @ApiOperation(value = "新增账号")
    @PostMapping(value = "/add")
    public boolean add(@RequestBody AddEnterpriseAccountVO enterpriseAccountVO) {

        //bean拷贝
        EnterpriseAccount enterpriseAccount = new EnterpriseAccount();

        BeanUtils.copyProperties(enterpriseAccountVO, enterpriseAccount);

        //设置时间
        LocalDateTime localDateTime = LocalDateTime.now();

        enterpriseAccount.setApplicationTime(localDateTime);

        LocalDateTime expireTime = null;

        //试用期默认7天
        if (enterpriseAccountVO.getStatus() == 0) {
            expireTime = localDateTime.plusDays(7);
        }

        if (enterpriseAccountVO.getStatus() == 1) {
            //设置到期时间
            expireTime = localDateTime.plusDays(enterpriseAccountVO.getValidityDay());
        }

        if (expireTime != null) {
            enterpriseAccount.setExpireTime(expireTime);
        } else {
            throw new RuntimeException("帐号类型信息设置有误");
        }

        return enterpriseAccountService.add(enterpriseAccount);
    }

    /**
     * 根据id查询
     */
    @ApiOperation(value = "账户查看")
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键", required = true, dataType = "String")
    @GetMapping(value = "/getById/{id}")
    public EnterpriseAccount getById(@PathVariable("id") String id) {
        return enterpriseAccountService.getById(id);
    }

    /**
     * 账号编辑
     */
    @ApiOperation(value = "账号编辑")
    @PutMapping(value = "/update")
    public Result update(@RequestBody UpdateEnterpriseAccountVO updateEnterpriseAccountVO) {

        //查询原有企业账户信息
        EnterpriseAccount enterpriseAccount = enterpriseAccountService.getById(updateEnterpriseAccountVO.getEnterpriseId());

        Result result = new Result();
        if (enterpriseAccount == null) {
            result.setStatus(ResultCode.error);
            result.setDesc("修改账户不存在");
            return result;
        }

        //修改状态校验
        if (updateEnterpriseAccountVO.getStatus() != null) {
            //正式期不能修改成试用期
            if (updateEnterpriseAccountVO.getStatus() == 0 && enterpriseAccount.getStatus() == 1) {
                result.setStatus(ResultCode.error);
                result.setDesc("不能将正式帐号改为试用帐号");
                return result;
            }

            //试用改正式
            if (updateEnterpriseAccountVO.getStatus() == 1 && enterpriseAccount.getStatus() == 0) {
                //到期时间
                LocalDateTime now = LocalDateTime.now();
                //到期时间
                LocalDateTime expireTime = now.plusDays(updateEnterpriseAccountVO.getValidityDay());
                enterpriseAccount.setApplicationTime(now);
                enterpriseAccount.setExpireTime(expireTime);
            }

            //正式改延期
            if (updateEnterpriseAccountVO.getStatus() == 1 && enterpriseAccount.getStatus() == 1) {
                LocalDateTime now = LocalDateTime.now();
                //到期时间
                LocalDateTime expireTime = now.plusDays(updateEnterpriseAccountVO.getValidityDay());
                enterpriseAccount.setExpireTime(expireTime);
            }
        }

        //其他字段设置
        BeanUtils.copyProperties(updateEnterpriseAccountVO, enterpriseAccount);

        //执行修改
        boolean flag = enterpriseAccountService.updateById(enterpriseAccount);
        if (flag) {
            //修改成功
            result.setStatus(ResultCode.success);
            result.setDesc("修改成功");
            return result;
        } else {
            //修改失败
            result.setStatus(ResultCode.error);
            result.setDesc("修改失败");
            return result;
        }
    }
}