package com.haothink.common.utils.encrypt;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密解密类
 *
 * @author wanghao
 * @version 1.0 2011/04/23
 */
public final class SHAUtils {

    private static final Logger logger = LoggerFactory.getLogger(SHAUtils.class);

    public SHAUtils() {
    }

    public static String SHA1(String origin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(origin.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA1 加密出错!originKey:" + origin);
        }
        return StringUtils.EMPTY;
    }


}
