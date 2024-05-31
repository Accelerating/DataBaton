package io.databaton.crypt.impl;

import io.databaton.config.DataBatonConfig;
import io.databaton.config.DataBatonCryptConfig;
import io.databaton.crypt.CryptProcessor;
import io.databaton.enums.CryptType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * aes crypt processor
 * @author zxx
 */
@Slf4j
@Component(CryptProcessor.BEAN_PREFIX + CryptType.AES)
@ConditionalOnProperty(prefix = "databaton", name = "crypt.code", havingValue = CryptType.AES)
public class AesCryptProcessor implements CryptProcessor {
    /**
     * ECB (Electronic Code Book)
     */
    public static final String ECB = "ECB";
    /**
     * CBC (Cipher Block Chaining)
     */
    public static final String CBC = "CBC";
    /**
     * CFB (Cipher FeedBack)
     */
    public static final String CFB = "CFB";
    /**
     * OFB (Output FeedBack)
     */
    public static final String OFB = "OFB";
    /**
     * CTR (Counter)
     */
    public static final String CTR = "CTR";
    /**
     * GCM (Galois/Counter Mode)
     */
    public static final String GCM = "GCM";


    private final DataBatonCryptConfig cryptConfig;

    public AesCryptProcessor(DataBatonConfig batonConfig) {
        this.cryptConfig = batonConfig.getCrypt();
    }

    @Override

    public byte[] encrypt(byte[] rawData) {
        try{
            SecretKey secretKey = generateSecretKey(cryptConfig.getKey());
            Cipher cipher = Cipher.getInstance(cryptConfig.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(rawData);
        }catch (Exception e){
            log.error("aes encrypt failed", e);
        }
        return null;
    }

    public byte[] decrypt(byte[] cipherData){
        try{
            SecretKey secretKey = generateSecretKey(cryptConfig.getKey());
            Cipher cipher = Cipher.getInstance(cryptConfig.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(cipherData);
        }catch (Exception e){
            log.error("aes decrypt failed", e);
        }
        return null;
    }



    private SecretKey generateSecretKey(String key){
        try{
            int length = key.length();
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(key.getBytes(StandardCharsets.UTF_8));
            keyGenerator.init(length * 8, random);
            return keyGenerator.generateKey();
        }catch (Exception e){
            log.error("generate aes secret key failed", e);
        }
        return null;
    }

}
