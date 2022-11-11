package com.wang.compress;


import com.wang.extension.SPI;

/**
 * @author happytsing
 */

@SPI
public interface Compressor {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}
