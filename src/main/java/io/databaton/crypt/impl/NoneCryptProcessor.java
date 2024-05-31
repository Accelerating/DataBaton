package io.databaton.crypt.impl;

import io.databaton.crypt.CryptProcessor;
import io.databaton.enums.CryptType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * a crypt processor skip encrypt and decrypt
 * @author zxx
 */
@Component(CryptProcessor.BEAN_PREFIX + CryptType.NONE)
@ConditionalOnProperty(prefix = "databaton", name = "crypt.code", havingValue = CryptType.NONE)
public class NoneCryptProcessor implements CryptProcessor {
    @Override
    public byte[] encrypt(byte[] rawData) {
        return rawData;
    }

    @Override
    public byte[] decrypt(byte[] cipherData) {
        return cipherData;
    }
}
