package com.restkeeper.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.utils.Result;

import java.util.List;

public interface IStoreManagerService extends IService<StoreManager> {

    //分页查询
    IPage<StoreManager> queryPageByCriteria(int pageNo, int pageSize, String criteria);

    //增加门店管理员
    boolean addStoreManager(String name, String phone, List<String> storeIds);

    //修改门店管理员
    boolean updateStoreManager(String storeManagerId, String name, String phone, List<String> storeIds);

    //店长逻辑删除
    boolean deleteStoreManager(String storeManagerId);

    //店长暂停
    boolean pauseStoreManager(String storeManagerId);

    //登录
    Result login(String shopId, String phone, String loginPass);
}
