package com.alibaba.csb.sdk;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;

/**
 * Created by wiseking on 18/1/8.
 */
public class CmdHttpCaller {
  private static final String SDK_VERSION = "1.1.4";
  public static Options opt = new Options();

  static {
    opt.addOption("url", true, "请求地址，e.g: http://broker-ip:8086/CSB?p1=v1");
    opt.addOption("api", true, "服务名");
    opt.addOption("version", true, "服务版本");
    opt.addOption("ak", true, "accessKey, 可选");
    opt.addOption("sk", true, "secretKey, 可选");
    opt.addOption("method", true, "请求类型, 默认get, 可选的值为: get, post, cget和cpost");
    opt.addOption("proxy", true, "设置代理地址, 格式: proxy_hostname:proxy_port ");
    opt.addOption("H", true, "http header, 格式: -H \"key:value\"");
    opt.addOption("D", true, "请求参数, 格式: -D \"key=value\"");
    opt.addOption("cbJSON", true, "以JSON串方式post发送的请求body, 例如: -cbJSON '{\"name\":\"wiseking\"}'");
    opt.addOption("nonce", false, "-nonce 是否做nonce防重放处理，不定义为不做nonce重放处理");
    opt.addOption("h", "help", false, "打印帮助信息");
    opt.addOption("d", "debug", false, "打印调试信息");
  }

  //TODO: move to  common
  private static boolean isEmpty(String str) {
    return str == null || str.isEmpty();
  }

  public static void main(String[] args) {
    CommandLineParser parser = new DefaultParser();

    Boolean isDebug = false;
    try {
      CommandLine commandline = parser.parse(opt, args);
      if (commandline.getOptions().length == 0 && commandline.getArgs().length > 0) {
        //use old style cmd line
        CmdCaller.main(args);
        return;
      }
      if (commandline.hasOption("h")) {
        usage(null);
        return;
      }

      String ak = commandline.getOptionValue("ak");
      String sk = commandline.getOptionValue("sk");
      String api = commandline.getOptionValue("api");
      String version = commandline.getOptionValue("version");
      String method = commandline.getOptionValue("method");
      String[] headers = commandline.getOptionValues("H");
      String[] params = commandline.getOptionValues("D");
      String url = commandline.getOptionValue("url");
      String proxy = commandline.getOptionValue("proxy");
      String cbJSON = commandline.getOptionValue("cbJSON");
      boolean nonce = commandline.hasOption("nonce");
      isDebug = commandline.hasOption("d");

      if (isDebug) {
        System.out.println("url=" + url);
        System.out.println("api=" + api);
        System.out.println("version=" + version);
        System.out.println("ak=" + ak);
        System.out.println("sk=" + sk);
        System.out.println("proxy=" + proxy);
        System.out.println("nonce=" + nonce);
        printKV("HTTP Headers", headers);
        printKV("HTTP Params", params);
      }

      if (isEmpty(api)){
        usage("请定义 -api 参数");
        return;
      }

      if (isEmpty(version)){
        usage("请定义 -version 参数");
        return;
      }

      if (isEmpty(url)){
        usage("请定义 -url 参数");
        return;
      }

      if (method == null) {
        method = "get";
      }
      HttpParameters.Builder builder = HttpParameters.newBuilder();
      builder.api(api).version(version).method(method).requestURL(url).accessKey(ak).secretKey(sk);

      if (headers != null) {
        for (String header : headers) {
          String[] kv = header.split(":", 2);
          if (kv == null || kv.length != 2) {
            System.out.println("错误的HTTP头定义 正确格式: -H \"key:value\" !!" + header);
            return;
          }
          builder.putHeaderParamsMap(kv[0], kv[1]);
        }
      }

      if (params != null) {
        for (String param : params) {
          String[] kv = param.split("=", 2);
          if (kv == null || kv.length != 2) {
            System.out.println("错误的参数对定义 正确格式: -D \"key=value\" !!" + param);
            return;
          }
          builder.putParamsMap(kv[0], kv[1]);
        }
      }

      if (cbJSON != null) {
        if("cget".equalsIgnoreCase(method) || "cget".equalsIgnoreCase(method)) {
          System.out.println("当定义-cbJSON请求参数时， -method 必须为post方式!");
          return;
        }

        builder.contentBody(new ContentBody(cbJSON));
      }

      builder.nonce(nonce);

      boolean curlOnly = false;
      if (method.toLowerCase().startsWith("c")) {
        curlOnly = true;
        HttpCaller.setCurlResponse(true);
      }

      StringBuffer resHttpHeaders = new StringBuffer();
      //set http proxy
      if (proxy != null) {
        String errMsg = String.format("错误的proxy代理设置 %s 正确格式: -proxy \"proxy_host:proxy_port\" !!", proxy);
        String[] pcs = proxy.split(":");
        if (pcs == null || pcs.length != 2) {
          System.out.println(errMsg);
          return;
        }
        try {
          HttpCaller.setProxyHost(pcs[0], Integer.parseInt(pcs[1]), null);
        }catch (Exception e) {
          System.out.println(errMsg);
          return;
        }
      }

      String ret = HttpCaller.invoke(builder.build(), resHttpHeaders);

      if (curlOnly) {
        System.out.println("---- curlString = " + ret);
      } else {
        System.out.println("---- response http headers = " + resHttpHeaders.toString());
        System.out.println("---- retStr = " + ret);
        System.out.println("\n---- retStr after changeCharset = " + HttpCaller.changeCharset(ret));

        //call multi-times for stress or flow-ctrl testing
        int times = Integer.getInteger("test.stress.times", 0);
        for (int i = 2; i <= times; i++) {
          ret = HttpCaller.invoke(builder.build(), null);
          System.out.println("---- retStr [#" + i + "] = " + ret);
        }
      }
    } catch (Exception e) {
      System.out.println("-- 操作失败：" + e.getMessage());
      if (isDebug)
        e.printStackTrace(System.out);
    }
  }

  private static void printKV(String title, String[] kvs) {
    if (kvs != null) {
      System.out.println("---- " + title + " ----");
      for (String kv : kvs) {
        System.out.print(kv);
      }
      System.out.println("---- ---- ----");
    }
  }

  static void usage(String message) {
    if (message != null)
      System.out.println("参数错误:" + message);

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar http-client.jar [options...]", opt);
    System.out.println("\ncurrent SDK version:" + SDK_VERSION + "\n----");
    try {
      System.out.println(CommUtil.geCurrenttVersionFile());
    } catch (IOException e) {
      //
    }
    System.exit(0);
  }

}
