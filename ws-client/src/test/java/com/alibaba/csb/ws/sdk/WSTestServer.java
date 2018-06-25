package com.alibaba.csb.ws.sdk;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import javax.xml.ws.http.HTTPBinding;
import java.io.StringReader;

/**
 * startup a WS service as http://localhost:9081
 * Created by wiseking on 18/6/25.
 */
public class WSTestServer {
  public static void main(String[] args) throws InterruptedException {
    String address = "http://0.0.0.0:9081/wiseking";
    Endpoint.create(HTTPBinding.HTTP_BINDING, new HttpServer()).publish(address);

    System.out.println("Service running at " + address);
    System.out.println("Type [CTRL]+[C] to quit!");

    Thread.sleep(Long.MAX_VALUE);
  }

  @WebServiceProvider
  @ServiceMode(value = Service.Mode.MESSAGE)
  public static class HttpServer implements Provider<Source> {

    public Source invoke(Source request) {
      System.out.println("request come");
      return new StreamSource(new StringReader("<p>Hello world!</p>" + '\n'));
    }
  }
}
