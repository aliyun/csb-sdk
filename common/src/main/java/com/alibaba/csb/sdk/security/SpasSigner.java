package com.alibaba.csb.sdk.security;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;

public class SpasSigner {
    public enum SigningAlgorithm {
        HmacSHA1, HmacSHA256, HmacMD5;
    }

    public static class SDKSecurityException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public SDKSecurityException(String msg, Exception e) {
            super(msg, e);
        }
    }

    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static String sign(String data, String key) {
        return signAndBase64Encode(data, key, SigningAlgorithm.HmacSHA1);
    }

    public static String sign(String data, String key, SigningAlgorithm algorithm) {
        return signAndBase64Encode(data, key, algorithm);
    }

    public static String sign(byte[] data, String key) {
        return signAndBase64Encode(data, key, SigningAlgorithm.HmacSHA1);
    }

    public static String sign(byte[] data, String key, SigningAlgorithm algorithm) {
        return signAndBase64Encode(data, key, algorithm);
    }

    public static String sign(SortedParamList parameters, String key) {
        return sign(parameters, key, SigningAlgorithm.HmacSHA1);
    }

    public static String sign(SortedParamList parameters, String key, SigningAlgorithm algorithm) {
        String data = parameters.toString();
        return sign(data, key, algorithm);
    }

    private static String signAndBase64Encode(String data, String key, SigningAlgorithm algorithm)
            throws SDKSecurityException {
        try {
            byte[] signature = sign(data.getBytes(UTF8), key.getBytes(UTF8), algorithm);
            return new String(Base64.encodeBase64(signature));
        } catch (Exception e) {
            throw new SDKSecurityException("Unable to calculate a request signature: " + e.getMessage(), e);
        }
    }

    private static String signAndBase64Encode(byte[] data, String key, SigningAlgorithm algorithm)
            throws SDKSecurityException {
        try {
            byte[] signature = sign(data, key.getBytes(UTF8), algorithm);
            return new String(Base64.encodeBase64(signature));
        } catch (Exception e) {
            throw new SDKSecurityException("Unable to calculate a request signature: " + e.getMessage(), e);
        }
    }

    private static byte[] sign(byte[] data, byte[] key, SigningAlgorithm algorithm) throws SDKSecurityException {
        try {
            Mac mac = Mac.getInstance(algorithm.toString());
            mac.init(new SecretKeySpec(key, algorithm.toString()));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new SDKSecurityException("Unable to calculate a request signature: " + e.getMessage(), e);
        }
    }
}
