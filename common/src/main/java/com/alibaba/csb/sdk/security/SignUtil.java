package com.alibaba.csb.sdk.security;

import com.alibaba.csb.sdk.CsbSDKConstants;
import com.alibaba.csb.security.spi.SignServiceRuntime;

import java.util.*;

/**
 * SignUtil for signing http parameters 
 * @author liaotian.wq 2017年1月20日
 *
 */
public class SignUtil {
	public static Map<String, String> newParamsMap(final Map<String, List<String>> paramsMap, String apiName, String version,
																								 String accessKey, String securityKey, boolean timestampFlag, boolean nonceFlag,
																								 final Map<String, String> extSignHeaders, final StringBuffer signInfo, String signSPI) {
		return SignServiceRuntime.pickSignService(signSPI).signParamsMap(
				paramsMap,apiName,version, accessKey, securityKey, timestampFlag, nonceFlag, signInfo, extSignHeaders);
	}
}
