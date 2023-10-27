package com.restkeeper.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.utils.Result;

import java.util.List;

public interface IStoreService extends IService<Store> {

    //分页查询（根据门店名称进行模糊查询）
    IPage<Store> queryPageByName(int pageNo, int pageSize, String name);

    //查询省份列表信息
    List<String> getAllProvince();


    //根据省份信息查询门店列表
    List<StoreDTO> getStoreByProvince(String province);

    //根据当前登录的管理员id获取与之相关的门店列表
    List<StoreDTO> getStoreListByManagerId();

    //切换门店
    Result switchStore(String storeId);

}
