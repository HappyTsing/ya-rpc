package com.wang.dto;


import com.wang.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

/**
 * @author happytsing
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {

    /**
     * requestId
     * code 响应码
     * message 提示信息
     * data 响应数据
     */
    private String requestId;
    private Integer code;
    private String message;
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        return RpcResponse.<T>builder()
                .requestId(requestId)
                .code(RpcResponseCodeEnum.SUCCESS.getCode())
                .message(RpcResponseCodeEnum.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    public static <T> RpcResponse<T> fail() {
        return RpcResponse.<T>builder()
                .code(RpcResponseCodeEnum.FAIL.getCode())
                .message(RpcResponseCodeEnum.FAIL.getMessage())
                .build();
    }

}

