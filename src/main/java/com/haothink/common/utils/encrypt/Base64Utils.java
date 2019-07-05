package com.haothink.common.utils.encrypt;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 加密解密类
 *
 * @author wanghao
 * @version 1.0 2011/04/23
 */
public final class Base64Utils {

    private static final Logger logger = LoggerFactory.getLogger(Base64Utils.class);


    private static final Base64 BASE_64 = new Base64();

    public Base64Utils() {
    }

    /**
     * 使用Base64加密
     */
    public static String base64Encode(String plainText) {
        if (StringUtils.isBlank(plainText)) {
            return null;
        }
        return new String(BASE_64.encode(plainText.getBytes()));
    }

    /**
     * 使用Base64加密
     */
    public static String base64Encode(byte[] bytes) {
        return new String(BASE_64.encode(bytes));
    }

    /**
     * 使用Base64解密
     */
    public static String base64Decode(String encodeStr) {
        if (StringUtils.isBlank(encodeStr)) {
            return null;
        }
        return new String(BASE_64.decode(encodeStr.getBytes()));
    }

    /**
     * 使用Base64解密
     */
    public static String base64Decode(byte[] bytes) {
        return new String(BASE_64.decode(bytes));
    }

}
