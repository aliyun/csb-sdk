package com.alibaba.csb.ws.sdk;

import java.io.ByteArrayOutputStream;

import javax.xml.soap.SOAPMessage;

/**
 * A helper class to dump SOAPMessage as a String.
 * @author liaotian.wq
 *
 */
public class DumpSoapUtil {
	/**
	 * Help method to dump soapMessage as a xml String
	 * @param promptMsg    prompt info
	 * @param soapMessage  the soap message be dump and printed
	 * @return
	 */
	public static String dumpSoapMessage(String promptMsg, SOAPMessage soapMessage)  {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			soapMessage.writeTo(baos);
		} catch (Exception e) {
		}
		String rtn = baos.toString();
		
		return rtn;
	}
	
	public static String dumpSoapMessage(SOAPMessage soapMessage)  {
		return dumpSoapMessage(null, soapMessage);
	}
}
