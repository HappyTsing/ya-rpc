package com.wang.dto;

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
public class RpcRequest implements Serializable {
    /**
     * requestId 请求 ID
     * interfaceName 接口名
     * methodName 方法名
     * params 参数列表
     * paramTypes 参数类型列表
     * version 接口版本，当接口出现不兼容的升级时，采用 version 标记区分。
     * group 接口组，当接口有多种实现时，采用 group 标记区分分。
     */
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] params;
    private Class<?>[] paramTypes;
    private String group;
    private String version;

    public ServiceSignature getServiceSignature() {
        return new ServiceSignature(interfaceName,group,version);
    }
}