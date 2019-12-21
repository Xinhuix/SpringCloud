package com.visionvera.util;


import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * aes加密
 * @author wang
 *
 */
public class AesUtilsWSM {
	private static Logger logger = LogManager.getLogger(AesUtilsWSM.class);
	private static String aesIv = "W@34*bHb#cUcd%d7";
	private static String aesKey = "1V3$5T78*23M5a7^";
	
	
	/**
	 * 功能的简述.
	 * <br>修改历史：
	 * <br>修改日期  修改者 BUG小功能修改申请单号
	 *
	 * @param args 注意：
	 * @throws Exception
	 */
	
	public static void main(String[] args) throws Exception {
	    String test = "[{\"Msg\":\"illegal json\"}]";
	    String test1 = "{\"Msg\":\"illegal json\"}";
	    String test2 = "dK9VhsdJBI7GKXajhskHNMjmXKasuFOmWv8o91CpZDU4/64Og+qDV4Ro4RS04AqQ";
	    byte[] encrypted=  Base64.decodeBase64(test2.getBytes("UTF-8"));
	    
	    System.out.println(Arrays.toString(encrypt(test)));
	    System.out.println(Arrays.toString(encrypt(test)));
	    System.out.println(Arrays.toString(encrypt(test1)));
	    System.out.println(decrypt(encrypt(test)));
	    System.out.println(decrypt(encrypt(test1)));
	    System.out.println(decrypt(encrypted));
	
	
	}
	
	/**
	 * aes加密
	 * @param content
	 * @return
	 */
	public static byte[] encrypt(String content) {
	    try {
	        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
	        byte[] encrypted = cipher.doFinal(content.getBytes("UTF-8"));
	        return encrypted;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	
	    return null;
	}
	
	private static Cipher getCipher(int encryptMode) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
	    byte[] raw = aesKey.getBytes("UTF-8");
	SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    IvParameterSpec iv = new IvParameterSpec(aesIv.getBytes());
	    cipher.init(encryptMode, skeySpec, iv);
	    return cipher;
	}
	
	/**
	 * aes 解密
	 *
	 * @param content
	 * @return
	 */
	public static String decrypt(byte[] content) {
	    try {
	        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
	        byte[] original = cipher.doFinal(content);
	        String originalString = new String(original,"UTF-8");
	    return originalString;
	} catch (Exception e) {
	    logger.error("AesUtilsWSM-----decrypt------", e);
	        return null;
	    }
	}
	
	
//	public static void main(String[] args) {
//		String a = "123456";
//		byte[] aa = encrypt(a);
//		String s1 = new String(aa);	
//		System.out.println("s1:"+s1);
//		String s2 = decrypt(aa);
//		System.out.println("s2:"+s2);		
//	}
	
	
}
