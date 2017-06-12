package com.alibaba.csb.sdk.util;

import java.io.ByteArrayOutputStream;

import javax.xml.soap.SOAPMessage;

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
		System.out.println(String.format("%s  %s", promptMsg, rtn));
		
		return rtn;
	}
}
