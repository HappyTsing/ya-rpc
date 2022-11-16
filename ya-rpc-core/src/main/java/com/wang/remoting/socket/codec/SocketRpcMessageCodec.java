package com.wang.remoting.socket.codec;

import com.wang.compress.Compressor;
import com.wang.consts.ProtocolConst;
import com.wang.dto.RpcMessage;
import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.enums.CompressorTypeEnum;
import com.wang.enums.MessageTypeEnum;
import com.wang.enums.SerializerTypeEnum;
import com.wang.exception.ProtocolException;
import com.wang.extension.ExtensionLoader;
import com.wang.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Arrays;

/**
 * 自定义协议
 * <pre>
 *   0     1     2     3     4         5    6    7    8      9            10            11         12   13   14   15  16
 *   +-----+-----+-----+-----+---------+----+----+----+-----+--------------+------------+------------+---+---+---+---+
 *   |      magic code       | version |     body length     | messageType | serializer | compressor | extension bit |
 *   +-----------------------+---------+--------------------+--------------+------------+------------+---------------+
 *   |                                                                                                               |
 *   |                                             body                                                              |
 *   |                                                                                                               |
 *   |                                            ... ...                                                            |
 *   +---------------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）     1B  version（版本）         4B  body length（body长度）
 * 1B  message type（消息类型）  1B  compressor（压缩类型）   1B  serializer（序列化类型）
 * 4B  extension bits（扩展位）
 * body（RpcRequest 或者 RpcResponse 实例对象）
 * </pre>
 */
@Slf4j
public class SocketRpcMessageCodec {

    public SocketRpcMessageCodec(){
    }

    /**
     * 基于协议消息，对数据进行序列化、压缩。
     * 按照协议输出 magic code、version、body length、message type、serializer type、compressor type、body
     * @param rpcMessage 协议消息
     * @param dout 输出流
     */
    public void encode(RpcMessage rpcMessage, DataOutputStream dout) throws IOException {
        log.info("Encoding protocol.");
        byte messageType = rpcMessage.getMessageType();
        byte serializerType = rpcMessage.getSerializeType();
        byte compressorType = rpcMessage.getCompressType();
        Object data = rpcMessage.getData();
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(SerializerTypeEnum.getName(serializerType));
        Compressor  compressor= ExtensionLoader.getExtensionLoader(Compressor.class).getExtension(CompressorTypeEnum.getName(compressorType));

        byte[] body = serializer.serialize(data);
        body = compressor.compress(body);
        int bodyLength = body.length;

        // 输出协议
        dout.write(ProtocolConst.MAGIC_CODE);
        dout.write(ProtocolConst.VERSION);
        dout.writeInt(bodyLength);
        dout.write(messageType);
        dout.write(serializerType);
        dout.write(compressorType);
        dout.write(body);
        dout.flush();
    }

    /**
     * 首先判断 magic code 和 version 是否符合
     * 随后，根据约定的协议，从输入流中读取协议头的各种信息。
     * 最后，读出 body，对其进行解压缩和反序列化。
     * @param din 输入流
     * @return 基于 messageType 的不同，返回 RpcRequest/RpcResponse
     */
    public Object decode(DataInputStream din) throws IOException {
        log.info("Decoding protocol.");
        checkMagicCode(din);
        checkVersion(din);
        int bodyLength = din.readInt();
        byte messageType = din.readByte();
        byte serializerType = din.readByte();
        byte compressorType = din.readByte();
        byte[] body = new byte[bodyLength];
        din.read(body);

        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(SerializerTypeEnum.getName(serializerType));
        Compressor compressor = ExtensionLoader.getExtensionLoader(Compressor.class).getExtension(CompressorTypeEnum.getName(compressorType));
        byte[] data = compressor.decompress(body);
        if(messageType == MessageTypeEnum.REQUEST.getCode()){
            RpcRequest rpcRequest = serializer.deserialize(data, RpcRequest.class);
            return rpcRequest;
        }else if(messageType == MessageTypeEnum.RESPONSE.getCode()){
            RpcResponse rpcResponse = serializer.deserialize(data, RpcResponse.class);
            return rpcResponse;
        }else {
            throw new java.net.ProtocolException("Error MessageType: "+ MessageTypeEnum.getName(messageType));
        }
    }

    /**
     * 检查 magic code 是否符合，用于快速初步判断输入流是否是约定的协议。
     */
    private void checkMagicCode(DataInputStream din) throws IOException {
        int magicCodeLength = ProtocolConst.MAGIC_LENGTH;
        byte[] magicCode = new byte[magicCodeLength];
        din.read(magicCode);
        for (int i = 0; i < magicCodeLength; i++) {
            if (magicCode[i] != ProtocolConst.MAGIC_CODE[i]) {
                throw new ProtocolException("Unknown magic code: " + Arrays.toString(magicCode));
            }
        }
        log.info("Check magic code success.");
    }

    /**
     * 检查协议版本是否符合
     */
    private void checkVersion(DataInputStream din) throws IOException{
        int versionLength = ProtocolConst.VERSION_LENGTH;
        assert  versionLength == 1;
        byte version = din.readByte();
        if(version!=ProtocolConst.VERSION){
            throw new ProtocolException("Incompatible version: "+ version + " It should be: " + ProtocolConst.VERSION);
        }
        log.info("Check protocol version success.");
    }
}
