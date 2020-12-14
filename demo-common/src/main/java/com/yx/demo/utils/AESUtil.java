package com.yx.demo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

/**
 * 中进专属
 * Created by keegan on 17/08/2017.
 */
public class AESUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AESUtil.class);
    private final static String CHARSET = "UTF-8";
    private final static String AES = "AES";
    private final static String PADDING = "AES/CBC/PKCS5Padding";

    public static String passwordEncrypt(String password, String secret) throws Throwable {
        return URLEncoder.encode(encrypt(URLEncoder.encode(password, CHARSET), secret.substring(0, 16), secret.substring(16)), CHARSET);
    }

    public static String passwordDecrypt(String password, String appSecret) throws Throwable {
        return URLDecoder.decode(decrypt(password, appSecret.substring(0, 16), appSecret.substring(16)), CHARSET);
    }

    /**
     * 加密
     *
     * @param content 加密文本
     * @param key     加密密钥，appSecret的前16位
     * @param iv      初始化向量，appSecret的后16位
     * @return
     * @throws Throwable
     */
    public static String encrypt(String content, String key, String iv) throws Throwable {
        byte[] raw = key.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, AES);
        Cipher cipher = Cipher.getInstance(PADDING); //"算法/模式/补码方式"
        IvParameterSpec ivParam = new IvParameterSpec(iv.getBytes(CHARSET)); //使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParam);
        byte[] encrypted = cipher.doFinal(content.getBytes(CHARSET));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 解密
     *
     * @param content 解密文本
     * @param key     加密密钥，appSecret的前16位
     * @param iv      初始化向量，appSecret的后16位
     * @return
     * @throws Throwable
     */
    public static String decrypt(String content, String key, String iv) throws Throwable {
        byte[] raw = key.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, AES);
        Cipher cipher = Cipher.getInstance(PADDING); //"算法/模式/补码方式"
        IvParameterSpec ivParam = new IvParameterSpec(iv.getBytes(CHARSET)); //使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParam);
        byte[] encrypted = Base64.getDecoder().decode(content); //先用base64解密
        byte[] original = cipher.doFinal(encrypted);
        return new String(original);
    }


    /**
     * 接口签名
     *
     * @param content
     * @param appSecret
     * @return
     */
    public static String encrypt(String content, String appSecret) {
        String hash = null;
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(appSecret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            hash = Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(content.getBytes()));
            hash = URLEncoder.encode(hash, "UTF-8");
        } catch (Exception e) {
            System.out.println("Error");
        }
        return hash;
    }


    public static void main(String[] args) {
        try {

//            String msg = "OBIR%2BlE254KjLoGLItDdGA%3D%3D";
//            System.out.println("password encrpt" + AESUtil.passwordDecrypt(msg, "S2MXWUL568HPWHHDMVZRWQH1V7KPTCB3"));
//            System.out.println(passwordDecrypt(msg,"S2MXWUL568HPWHHDMVZRWQH1V7KPTCB3"));
//            String secret="uZBZyKF212tgFpRJbMpgTNFS4I9fk0qa";
            String cardNo="1111222233334444555";
            String encrptStr=DESUtil.encrypt(cardNo);
            System.out.println("加密后: "+encrptStr);

            String decryptStr=DESUtil.decrypt(encrptStr);
            System.out.println("解密后: "+decryptStr);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
