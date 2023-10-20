package com.restkeeper.response;

import com.alibaba.fastjson.JSON;
import com.restkeeper.response.exception.ExceptionResponse;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.utils.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 返回结果统一转换
 */
@RestControllerAdvice(basePackages = "com.restkeeper")
public class ResponseAdvisor implements ResponseBodyAdvice<Object> {

    /**
     *  判断哪些需要拦截
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 返回结果包装
     * @param body controller返回的数据
     * @param returnType
     * @param selectedContentType
     * @param selectedConverterType
     * @param request
     * @param response
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        if (body instanceof Result){
            return body;
        }

        if (body instanceof String) {
            //这里注意是将result转换为json字符串
            String result =  JSON.toJSONString(body);
            return JSON.toJSONString(body);
        }

        if (body instanceof Boolean){
            boolean result = (boolean)body;
            return new BaseResponse<Boolean>(result);
        }

        if (body instanceof PageVO){
            return new BaseResponse<>(body);
        }


        if (body instanceof ExceptionResponse){
            return new BaseResponse<>(400,((ExceptionResponse)body).getMsg());
        }

        return new BaseResponse<>(body);
    }
}
