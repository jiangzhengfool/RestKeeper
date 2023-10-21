package com.restkeeper.operator.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.operator.entity.EnterpriseAccount;

public interface IEnterpriseAccountService extends IService<EnterpriseAccount> {

    /**
     * 根据名称分页查询
     *
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    IPage<EnterpriseAccount> queryPageByName(int pageNum, int pageSize, String enterpriseName);

    /**
     * 新增帐号
     *
     * @param account
     * @return
     */
    boolean add(EnterpriseAccount account);
}