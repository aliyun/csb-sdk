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

import com.alibaba.csb.sdk.CsbSDKConstants;
import com.alibaba.csb.sdk.security.SignUtil;
import static com.alibaba.csb.sdk.CsbSDKConstants.*;
import com.alibaba.csb.ws.sdk.WSClientException;
import com.alibaba.csb.ws.sdk.WSClientSDK;
import com.alibaba.csb.ws.sdk.WSParams;

public class SOAPHeaderHandler implements SOAPHandler<SOAPMessageContext>{
	private WSParams wsParams;
	private boolean dumpHeaders;

	public SOAPHeaderHandler(WSParams params) {
		this.wsParams = params;
		this.dumpHeaders = params.isDebug();
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
				if (wsParams.getAk() != null) {
					Map<String, String> headers = SignUtil.newParamsMap(null, wsParams.getApi(), wsParams.getVersion(),
							wsParams.getAk(), wsParams.getSk(), wsParams.isTimestamp(), wsParams.isNonce(), WSClientSDK.genExtHeader(wsParams.getFingerPrinter()));
					for (Entry<String,String> kv:headers.entrySet()) {
						header.addHeaderElement(new QName(HEADER_NS, kv.getKey())).setTextContent(kv.getValue());
						dumpHeaders(kv.getKey(), kv.getValue());
					}
				}

				if (wsParams.isMockRequest()) {
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
	
	
	public static Map<String, List<String>> genSecrectHeaders(WSParams params) {
		Map<String, String> requestHeaders = WSClientSDK.generateSignHeaders(params);

		Map<String, List<String>> rtn = new HashMap<String, List<String>>();
		for(Entry<String,String> kv:requestHeaders.entrySet()) {
			rtn.put(kv.getKey(), Arrays.asList(kv.getValue()));
		}
		
		if (params.isDebug()) {
			StringBuffer sb = new StringBuffer();
			for(Entry<String,String> kv:requestHeaders.entrySet()) {
				if (sb.length()>0) {
					sb.append(" ");
				}
				sb.append(kv.getKey()).append(":").append(kv.getValue()).append(";");
			}
			System.out.println("sign headers= " + sb.toString());
		}
		
		return rtn;
	}

}
