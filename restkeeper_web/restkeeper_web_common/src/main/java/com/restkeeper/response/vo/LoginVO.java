package com.restkeeper.response.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class LoginVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "登录账号")
	private String loginName;

	@ApiModelProperty(value = "密码")
	private String loginPass;
}