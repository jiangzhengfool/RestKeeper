package com.restkeeper.operator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restkeeper.operator.entity.EnterpriseAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 企业账号管理 Mapper 接口
 * </p>
 */
@Mapper
public interface EnterpriseAccountMapper extends BaseMapper<EnterpriseAccount> {

    /**
     * 数据还原
     *
     * @param id
     * @return
     */
    @Update("update t_enterprise_account set is_deleted=0 where enterprise_id=#{id} and is_deleted=1")
    boolean recovery(@Param("id") String id);

}