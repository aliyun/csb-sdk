package com.alibaba.csb.sdk.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SignUtil for signing http parameters 
 * @author liaotian.wq 2017年1月20日
 *
 */
public class SignUtil {
	/**
	 * convert parameter to Signature requried ParamNode format
	 * @param map
	 * @return
	 */
	private static List<ParamNode> convertSingleValueParms(Map<String, String> map) {
        List<ParamNode> pnList = new ArrayList<ParamNode>();

        String key;
        for(Map.Entry<String, String> entry : map.entrySet()) {
        	key = entry.getKey();
        	ParamNode node = new ParamNode(key, entry.getValue());
        	pnList.add(node);
        }

        return pnList;
    }

	/**
	 * convert parameter to Signature requried ParamNode format
	 * @param map
	 * @return
	 */
	private static List<ParamNode> convertMultiValueParams(Map<String, List<String>> map) {
        List<ParamNode> pnList = new ArrayList<ParamNode>();

        String key;
        for(Map.Entry<String, List<String>> entry : map.entrySet()) {
        	key = entry.getKey();
        	List<String> vlist = entry.getValue();
            if (vlist == null) {
                ParamNode node = new ParamNode(key, null);
                pnList.add(node);
            } else { 
            	for (String v:vlist) {
            		ParamNode node = new ParamNode(key, v);
            		pnList.add(node);
            	}
            };
        }

        return pnList;
    }
	
	/**
	 * Signature single value parameter list with security key
	 * @param paramNodeList
	 * @param secretKey
	 * @return
	 */
	public static String sign(Map<String, String> paramsMap, String secretKey)  {
		List<ParamNode> paramNodeList = convertSingleValueParms(paramsMap);
		return sign(paramNodeList, secretKey);
	}
	
	/**
	 * Signature multiple values parameter list with security key
	 * @param newParamsMap
	 * @param secretKey
	 * @return
	 */
	public static String signMultiValueParams(Map<String, List<String>> newParamsMap,String secretKey)  {
		List<ParamNode> paramNodeList = convertMultiValueParams(newParamsMap);
		return sign(paramNodeList, secretKey);
	}
    

    private static String sign(List<ParamNode> paramNodeList,String secretKey) 
    {
    	if (paramNodeList==null) {
    		paramNodeList = new ArrayList<ParamNode>();
    	}

        return SpasSigner.sign(paramNodeList, secretKey);
    }
}
