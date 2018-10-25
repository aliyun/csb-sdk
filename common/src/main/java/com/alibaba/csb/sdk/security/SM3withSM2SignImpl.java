package com.alibaba.csb.sdk.security;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton SM3withSM2 impl for SignService
 * Created by yingchuan.ctb on 18/10/25.
 */
public class SM3withSM2SignImpl extends DefaultSignServiceImpl {
    public static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    protected String sign(List<ParamNode> paramNodeList, String secretKey) {
        if (paramNodeList == null) {
            paramNodeList = new ArrayList<ParamNode>();
        }

        SortedParamList paramList = new SortedParamList();
        paramList.addAll(paramNodeList);
        String data = paramList.toString();
//        byte[] signature = sign(data.getBytes(UTF8), secretKey.getBytes(UTF8), SpasSigner.SigningAlgorithm.HmacSHA1);
        byte[] signature = "adfdf".getBytes(UTF8);
        return new String(Base64.encodeBase64(signature));
    }
}
