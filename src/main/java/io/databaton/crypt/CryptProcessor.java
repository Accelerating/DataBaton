package io.databaton.crypt;


public interface CryptProcessor {

    String BEAN_PREFIX = "crypt_processor_";

    byte[] encrypt(byte[] rawData);

    byte[] decrypt(byte[] cipherData);

    static String getBeanName(int cryptType){
        return BEAN_PREFIX + cryptType;
    }

}
