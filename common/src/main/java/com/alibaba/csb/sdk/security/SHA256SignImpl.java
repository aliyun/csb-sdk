package com.alibaba.csb.sdk.security;

import com.alibaba.csb.security.spi.SignService;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 客户端签名示例，本示例利用 HSHA256 算法对请求进行签名
 * 签名时用到的 data 是所有请求参数合成的字符串，key 是 用户填写的 sk
 * 签名结果会存放在 HTTP 请求的头部 _api_signature 中，发送给 broker
 * @author 泊闻
 */
public class SHA256SignImpl implements SignService {

    /**
     * 根据客户端请求，生成签名
     * @param paramNodeList 已排序的待签名请求参数key=values键值对列表
     * @param accessKey     进行签名的凭证识别码
     * @param secretKey     进行签名的安全码
     * @return 生成的签名
     */
    @Override
    public String generateSignature(SortedParamList paramNodeList, String accessKey, String secretKey) {
        String data = paramNodeList.toString();
        String key = secretKey;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
            return new String(Base64.encodeBase64(mac.doFinal(data.getBytes())));
        } catch (Exception e) {
            throw new RuntimeException("generate signature error.", e);
        }
    }

    /**
     * 一般情况下，一个签名实现类无需支持多种算法，此时此方法可直接调用上面的方法，无需用到 algorithm 参数。
     * 如果需要在内部支持多种算法，可以用到 algorithm 参数
     */
    @Override
    public String generateSignature(SortedParamList paramNodeList, String accessKey, String secretKey, SpasSigner.SigningAlgorithm algorithm) {
        return generateSignature(paramNodeList, accessKey, secretKey);
    }
}
