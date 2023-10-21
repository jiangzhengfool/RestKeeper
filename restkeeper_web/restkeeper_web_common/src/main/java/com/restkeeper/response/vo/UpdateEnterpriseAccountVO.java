package com.restkeeper.response.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateEnterpriseAccountVO extends AddEnterpriseAccountVO {

    @ApiModelProperty(value = "企业id")
    private String enterpriseId;
}
