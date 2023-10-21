package com.restkeeper.operator.controller;

import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.operator.service.IEnterpriseAccountService;
import com.restkeeper.response.vo.AddEnterpriseAccountVO;
import com.restkeeper.response.vo.PageVO;
import io.swagger.annotations.Api;
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
}