package com.haothink.common.utils.encrypt;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

/**
 * 加密解密类
 *
 * @author wanghao
 * @version 1.0 2011/04/23
 */
public final class MD5Utils {

    private static final Logger logger = LoggerFactory.getLogger(MD5Utils.class);


    private static final Base64 BASE_64 = new Base64();

    public MD5Utils() {
    }


    /**
     * MD5加密
     *
     * @param origin
     * @return
     */
    public static String md5(String origin) {
        String resultString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = dumpBytes(md.digest(origin.getBytes()));
        } catch (Exception e) {
            logger.error("MD5 加密出错!originKey:" + origin, e);
        }
        return resultString;
    }

    private static String dumpBytes(byte[] bytes) {
        int i;
        StringBuffer sb = new StringBuffer();
        for (i = 0; i < bytes.length; i++) {
            if (i % 32 == 0 && i != 0) {
                sb.append("\n");
            }
            String s = Integer.toHexString(bytes[i]);
            if (s.length() < 2) {
                s = "0" + s;
            }
            if (s.length() > 2) {
                s = s.substring(s.length() - 2);
            }
            sb.append(s);
        }
        return sb.toString();
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

    public static void main(String[] args) {
        String content = "Hello 世界";

        System.out.println("original:" + content);
    }
}
