package com.wang.compress.dummy;

import com.wang.compress.Compressor;

/**
 * 伪压缩器
 * 经某些序列化工具处理后，无需再压缩。
 * 经过测试，本项目使用 gzip 后，仍能压缩约 15%
 * @author happytsing
 */
public class DummyCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        return bytes;
    }
    @Override
    public byte[] decompress(byte[] bytes) {
        return bytes;
    }
}
