package com.alibaba.csb.ws.sdk;

import org.junit.Test;

public class CmdWsCallerTest {
    private static final String reqSoap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:test=\"http://ws2restful.PING.csb/\">\n"
            + "<soapenv:Header/>\n" + "<soapenv:Body>\n" + "   <test:ws2restful>\n" + "      <name>abc</name>\n"
            + "   </test:ws2restful>\n" + "</soapenv:Body>\n" + "</soapenv:Envelope>\n";

    private static final String reqSoap2 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://webservices.amazon.com/AWSECommerceService/2011-08-01\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <ns:CartAdd>\n" +
            "         <ns:MarketplaceDomain>a</ns:MarketplaceDomain>\n" +
            "      </ns:CartAdd>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    @Test
    public void testCmdWsCaller() {
        String[] args = {
                "-soap12", "true",
                "-ak", "ak",
                "-sk", "sk",
                "-api", "PING",
                "-version", "vcsb",
                "-ea", "http://11.239.187.178:9081/PING/vcsb/ws2restful",
                "-ns", "http://ws2restful.PING.csb/",
                "-sname", "PING",
                "-pname", "ws2restfulPortType",
                "-d",
                "-rd", reqSoap

        };
        CmdWsCaller.main(args);
    }

    @Test
    public void testCmdWsCaller2() {
        String[] args = {
                "-ak", "ak",
                "-sk", "sk",
                "-api", "PING",
                "-version", "vcsb",
                "-ea", "http://localhost:9081/PING/vcsb/ws2restful",
                "-ns", "http://webservices.amazon.com/AWSECommerceService/2011-08-01",
                "-sname", "AWSECommerceService",
                "-pname", "AWSECommerceServicePortType",
                "-action", "http://soap.amazon.com/CartAdd",
                "-d",
                "-rd", reqSoap2

        };
        CmdWsCaller.main(args);
    }
}
