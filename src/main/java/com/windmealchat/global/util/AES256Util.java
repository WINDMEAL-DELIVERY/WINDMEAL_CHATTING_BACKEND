package com.windmealchat.global.util;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AES256Util {

  @Value("${encrypt.alg}")
  private String alg;

  @Value("${encrypt.key}")
  private String encryptKey;

  @Value("${encrypt.iv}")
  private String iv;

  public String encrypt(String text) throws Exception{
    Cipher cipher = Cipher.getInstance(alg);
    SecretKeySpec keySpec = new SecretKeySpec(encryptKey.getBytes(), "AES");
    IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);
    byte[] encrypted = cipher.doFinal(text.getBytes("UTF-8"));
    // 암호화가 완료된 값에 대해서 base64로 인코딩을 해준다.
    return Base64.getEncoder().encodeToString(encrypted);
  }

  public String decrypt(String cipherText) throws Exception {
    Cipher cipher = Cipher.getInstance(alg);
    SecretKeySpec keySpec = new SecretKeySpec(encryptKey.getBytes(), "AES");
    IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);
    // 복호화하기 전에 디코딩해준다.
    byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
    byte[] decrypted = cipher.doFinal(decodedBytes);
    return new String(decrypted, "UTF-8");
  }
}
