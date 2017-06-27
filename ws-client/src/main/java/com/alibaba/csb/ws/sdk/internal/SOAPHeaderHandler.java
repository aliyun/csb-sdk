package com.alibaba.csb.ws.sdk.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import com.alibaba.csb.sdk.security.SignUtil;
import static com.alibaba.csb.sdk.CsbSDKConstants.*;
import com.alibaba.csb.ws.sdk.WSClientException;
import com.alibaba.csb.ws.sdk.WSClientSDK;

public class SOAPHeaderHandler implements SOAPHandler<SOAPMessageContext>{
	private String accessKey;
	private String securityKey;
	private String fingerStr;
	private String apiName;
	private String apiVersion;
	private boolean dumpHeaders;
	private boolean isMock;

	public SOAPHeaderHandler(String ak, String sk, String apiName, String apiVersion, String fingerStr, boolean isMock, boolean dumpHeaders) {
		this.accessKey = ak;
		this.securityKey = sk;
		this.apiName = apiName;
		this.apiVersion = apiVersion;
		this.isMock = isMock;
		this.fingerStr = fingerStr;
		this.dumpHeaders = dumpHeaders;
	}
	
	public static String generateSignature(String ak, String sk, String apiName, String apiVersion, String fingerStr, String timestamp) {
		// calculate signature
		Map<String, String> newParamsMap = new HashMap<String, String>();
		newParamsMap.put(ACCESS_KEY, ak);
		if (apiName != null) {
			newParamsMap.put(API_NAME_KEY, apiName);
			newParamsMap.put(VERSION_KEY, apiVersion);
		}
		newParamsMap.put(TIMESTAMP_KEY, timestamp);
		newParamsMap.put(HEADER_FINGERPRINT, fingerStr);
		String signature = SignUtil.sign(newParamsMap, sk);
		return signature;
	}
	
	private void dumpHeaders(String key, String text) {
		if(dumpHeaders)
		  System.out.println(String.format("<%s xmlns=\"%s\">%s</%s>", key, HEADER_NS, text, key));
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		try {
			Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			if (outboundProperty.booleanValue()) {
				SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
				
				SOAPHeader header = envelope.getHeader();
				if (header == null)
				  header = envelope.addHeader();
				if (accessKey != null) {
					header.addHeaderElement(new QName(HEADER_NS, ACCESS_KEY)).setTextContent(this.accessKey);
					dumpHeaders(ACCESS_KEY, accessKey);
					if (apiName != null) {
						header.addHeaderElement(new QName(HEADER_NS, API_NAME_KEY)).setTextContent(apiName);
						header.addHeaderElement(new QName(HEADER_NS, VERSION_KEY)).setTextContent(apiVersion);
						dumpHeaders(API_NAME_KEY, apiName);
						dumpHeaders(VERSION_KEY, apiVersion);
					}
					
					String timestamp = String.valueOf(System.currentTimeMillis());
					header.addHeaderElement(new QName(HEADER_NS, TIMESTAMP_KEY)).setTextContent(timestamp);
					dumpHeaders(TIMESTAMP_KEY, timestamp);
					header.addHeaderElement(new QName(HEADER_NS, HEADER_FINGERPRINT)).setTextContent(fingerStr);
					dumpHeaders(HEADER_FINGERPRINT, fingerStr);

					String signature = generateSignature(accessKey, securityKey, apiName, apiVersion, fingerStr, timestamp);
					header.addHeaderElement(new QName(HEADER_NS, SIGNATURE_KEY)).setTextContent(signature);
					dumpHeaders(SIGNATURE_KEY, signature);
				}

				if (isMock) {
					header.addHeaderElement(new QName(HEADER_NS, HEADER_MOCK));
					dumpHeaders(HEADER_MOCK, "");
				}
			} else {
				// remove the unnecessary response headers
				// SOAPEnvelope envelope =
				// context.getMessage().getSOAPPart().getEnvelope();

			}
		} catch (Exception e) {
			throw new WSClientException("failed to add soap header", e);
		}
		
		return true;

	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return false;
	}

	@Override
	public void close(MessageContext context) {
	}

	@Override
	public Set<QName> getHeaders() {
		//System.out.println("------------- getHeaders");
		return new TreeSet();
	}
	
	
	public static Map<String, List<String>> genSecrectHeaders(String accessKey, String secretKey, String apiName, String apiVersion, String fingerStr, boolean isMock, boolean dumpHeaders) {
		//Add HTTP request Headers
		Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
		
		String timestamp = String.valueOf(System.currentTimeMillis());
		requestHeaders.put(ACCESS_KEY, Arrays.asList(accessKey));
		requestHeaders.put(HEADER_FINGERPRINT, Arrays.asList(fingerStr));
		requestHeaders.put(TIMESTAMP_KEY, Arrays.asList(timestamp));
		if (apiName != null) {
			requestHeaders.put(API_NAME_KEY, Arrays.asList(apiName));
			requestHeaders.put(VERSION_KEY, Arrays.asList(apiVersion));
		}
		
		String signValue = generateSignature(accessKey, secretKey, apiName, apiVersion, fingerStr, timestamp);
		requestHeaders.put(SIGNATURE_KEY, Arrays.asList(signValue));
		
		if (isMock)
			requestHeaders.put(HEADER_MOCK, Arrays.asList("true"));
		
		if (dumpHeaders) {
			StringBuffer sb = new StringBuffer();
			for(Entry<String,List<String>> kv:requestHeaders.entrySet()) {
				if (sb.length()>0) {
					sb.append(" ");
				}
				sb.append(kv.getKey()).append(":").append(kv.getValue().get(0)).append(";");
			}
			System.out.println("sign headers= " + sb.toString());
		}
		
		return requestHeaders;
	}

}
