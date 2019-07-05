package com.haothink.common.utils.encrypt;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密utils AdvancedEncryptionStandard
 * Created by wanghao on 16/3/24.
 */
public class AesUtils{

    private static final String KEY = "B!r91345Aar12345"; // 128 bit key
    private static final String INIT_VECTOR = "RandomInitVector"; // 16 bytes IV

    private static final Logger logger = LoggerFactory.getLogger(AesUtils.class);

    /**
     * AES加密
     *
     * @param value
     * @return
     */
    public static String encrypt(String value) {
        return encrypt(KEY, INIT_VECTOR, value);
    }

    /**
     * AES解密
     *
     * @param encrypted
     * @return
     */
    public static String decrypt(String encrypted) {
        if (encrypted == null) {
            return null;
        }
        return decrypt(KEY, INIT_VECTOR, encrypted);
    }

    /**
     * 简单AES加密 无INIT_VECTOR
     * 一般对接第三方使用
     * @param key
     * @param value
     * @return
     */
    public static String basicEncrypt(String key, String value) {
        String initVector = null;
        return encrypt(key, initVector, value);
    }

    /**
     * 简单AES解密 无INIT_VECTOR
     * 一般对接第三方使用
     * @param key
     * @return
     */
    public static String basicDecrypt(String key, String encrypted) {
        String initVector = null;
        return decrypt(key, initVector, encrypted);
    }


    /**
     * 标准AES加密
     * @param key
     * @param initVector
     * @param value
     * @return
     */
    private static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = null;
            if (initVector != null) {
                iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            }
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            if (iv == null) {
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            }
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            logger.error("aes encrypt error!",ex);
        }

        return null;
    }

    /**
     * 标准AES解密
     * @param key
     * @param initVector
     * @param encrypted
     * @return
     */
    private static String decrypt(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv = null;
            if (initVector != null) {
                iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            }
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

            if (iv == null) {
                cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            }

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            logger.error("aes decrypt error!",ex);
        }

        return null;
    }

}

