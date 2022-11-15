package com.wang.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.wang.enums.SerializerTypeEnum;
import com.wang.enums.CompressorTypeEnum;
import com.wang.enums.MessageTypeEnum;
/**
 * 协议消息
 * @author happytsing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcMessage {
    /**
     * 消息类型 {@link MessageTypeEnum#getCode()}
     */
    private byte messageType;

    /**
     * 压缩类型 {@link CompressorTypeEnum#getCode()}
     */
    private byte compressType;

    /**
     * 序列化类型 {@link SerializerTypeEnum#getCode()}
     */
    private byte serializeType;


    /**
     * 消息数据，根据消息类型的不同，分为 {@link RpcResponse} 和 {@link RpcRequest}
     */
    private Object data;
}