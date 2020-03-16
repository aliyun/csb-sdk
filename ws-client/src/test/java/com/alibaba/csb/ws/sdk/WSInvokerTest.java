package com.alibaba.csb.ws.sdk;

import org.junit.Test;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;

/**
 *
 */
public class WSInvokerTest {

    @Test
    public void test() {
        String nameSpace = "http://webservices.amazon.com/AWSECommerceService/2011-08-01";
        String serviceName = "AWSECommerceService";
        String portName = "AWSECommerceServicePortType";
        String soapActionUri = "http://soap.amazon.com/CartAdd";
        boolean isSoap12 = false;
        String endpoint = "http://localhost:9081/csbTest/1.0.0/ws2ws";
        String reqSoap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws2=\"http://ws2ws.csbTest.csb/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <ws2:ws2ws>\n" +
                "         <pageSize>10</pageSize>\n" +
                "      </ws2:ws2ws>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        WSParams params = WSParams.create().api("csbTest").version("1.0.0").accessKey("ak1").secretKey("sk").fingerPrinter("wiseking");

        Dispatch<SOAPMessage> dispatch = WSInvoker.createDispatch(params, nameSpace, serviceName, portName, soapActionUri, isSoap12, endpoint);
        SOAPMessage request = WSInvoker.createSOAPMessage(isSoap12, reqSoap);

        SOAPMessage response = dispatch.invoke(request);
        System.out.println(response);
    }

}