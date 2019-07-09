package com.alibaba.csb.sdk;

import com.alibaba.csb.sdk.i18n.MessageHelper;
import org.apache.commons.cli.*;

/**
 * Created by wiseking on 18/1/8.
 */
public class CmdHttpCaller {
    private static final String SDK_VERSION = "1.1.5.3";

    public static Options opt = new Options();

    static {
        opt.addOption("url", true, "测试:" + MessageHelper.getMessage("cli.url"));
        opt.addOption("api", true, MessageHelper.getMessage("cli.api"));
        opt.addOption("version", true, MessageHelper.getMessage("cli.version"));
        opt.addOption("ak", true, MessageHelper.getMessage("cli.ak"));
        opt.addOption("sk", true, MessageHelper.getMessage("cli.sk"));
        opt.addOption("method", true, MessageHelper.getMessage("cli.method"));
        opt.addOption("proxy", true, MessageHelper.getMessage("cli.proxy"));
        opt.addOption("H", true, MessageHelper.getMessage("cli.h"));
        opt.addOption("D", true, MessageHelper.getMessage("cli.d"));
        opt.addOption("cbJSON", true, MessageHelper.getMessage("cli.cbJSON", "\"{'name':'wiseking'}\""));
        opt.addOption("nonce", false, MessageHelper.getMessage("cli.nonce"));
        opt.addOption("h", "help", false, MessageHelper.getMessage("cli.help"));
        opt.addOption("d", "debug", false, MessageHelper.getMessage("cli.debug"));
        opt.addOption("cc", "changeCharset", false, MessageHelper.getMessage("cli.change.charset"));
        opt.addOption("sdkv", "sdk-version", false, MessageHelper.getMessage("cli.sdk.version"));
        opt.addOption("sign", "signImpl", true, MessageHelper.getMessage("cli.signImpl"));
        opt.addOption("verify", "verifySignImpl", true, MessageHelper.getMessage("cli.verifySignImpl"));
        opt.addOption("bizId", true, MessageHelper.getMessage("cli.bizId"));
        opt.addOption("bizIdKey", true, MessageHelper.getMessage("cli.bizIdKey"));
    }

    //TODO: move to  common
    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    //  "-api" "item.add"
//  "-version" "1.0.0"
//  "-bizIdKey" "bizid"
//  "-bizId" "e48ffd7c1e7f4d07b7fc141f43503cb2"
//  "-method" "post"
//  "-H" "_inner_ecsb_trace_id:1e195a1915580754080541004d4957"
//  "-H" "_inner_ecsb_rpc_id:1.1"
//  "-H" "bizid:e48ffd7c1e7f4d07b7fc141f43503cb3"
//  "-D" "item={\"itemName\":\"benz\",\"quantity\":10}"
//  "-url" "http://11.167.131.193:8086/CSB"
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
            Boolean sdkv = commandline.hasOption("sdkv");
            String method = commandline.getOptionValue("method");
            String[] headers = commandline.getOptionValues("H");
            String[] params = commandline.getOptionValues("D");
            String url = commandline.getOptionValue("url");
            String proxy = commandline.getOptionValue("proxy");
            String cbJSON = commandline.getOptionValue("cbJSON");
            boolean nonce = commandline.hasOption("nonce");
            String bizIdKey = commandline.getOptionValue("bizIdKey");
            String bizId = commandline.getOptionValue("bizId");
            isDebug = commandline.hasOption("d");
            Boolean changeCharset = commandline.hasOption("cc");

            if (sdkv) {
                Version.version();
                return;
            }

            String signImpl = commandline.getOptionValue("sign");
            String verifySignImpl = commandline.getOptionValue("verify");

            if (isDebug) {
                System.out.println("url=" + url);
                System.out.println("api=" + api);
                System.out.println("version=" + version);
                System.out.println("bizIdKey=" + bizIdKey);
                System.out.println("bizId=" + bizId);
                System.out.println("ak=" + ak);
                System.out.println("sk=" + sk);
                System.out.println("proxy=" + proxy);
                System.out.println("nonce=" + nonce);
                System.out.println("signImpl=" + signImpl);
                System.out.println("verifySignImpl=" + verifySignImpl);
                printKV("HTTP Headers", headers);
                printKV("HTTP Params", params);
            }

            if (isEmpty(api)) {
                usage(MessageHelper.getMessage("cli.defparam", "-api"));
                return;
            }

            if (isEmpty(version)) {
                usage(MessageHelper.getMessage("cli.defparam", "-version"));
                return;
            }

            if (isEmpty(url)) {
                usage(MessageHelper.getMessage("cli.defparam", "-url"));
                return;
            }

            if (method == null) {
                method = "get";
            }
            HttpParameters.Builder builder = HttpParameters.newBuilder();
            if (bizIdKey != null && !bizIdKey.trim().equals("")) {
                HttpCaller.bizIdKey(bizIdKey);
            }
            builder.api(api).version(version).method(method).bizId(bizId).requestURL(url).accessKey(ak).secretKey(sk).signImpl(signImpl).verifySignImpl(verifySignImpl);

            if (headers != null) {
                for (String header : headers) {
                    String[] kv = header.split(":", 2);
                    if (kv == null || kv.length != 2) {
                        System.out.println("" + header);
                        return;
                    }
                    builder.putHeaderParamsMap(kv[0], kv[1]);
                }
            }

            if (params != null) {
                for (String param : params) {
                    String[] kv = param.split("=", 2);
                    if (kv == null || kv.length != 2) {
                        System.out.println(MessageHelper.getMessage("cli.defh", param));
                        return;
                    }
                    builder.putParamsMap(kv[0], kv[1]);
                }
            }

            if (cbJSON != null) {
                if ("cget".equalsIgnoreCase(method) || "get".equalsIgnoreCase(method)) {
                    System.out.println(MessageHelper.getMessage("cli.defpost"));
                    return;
                }
                if (cbJSON.startsWith("'")) {
                    System.out.println(MessageHelper.getMessage("cli.json.prefix"));
                    return;
                }
                builder.contentBody(new ContentBody(cbJSON));
            }

            builder.nonce(nonce);

            if (isDebug) {
                builder.diagnostic(true); //打印诊断信息
            }

            boolean curlOnly = false;
            if (method.toLowerCase().startsWith("c")) {
                curlOnly = true;
                HttpCaller.setCurlResponse(true);
            }

            StringBuffer resHttpHeaders = new StringBuffer();
            //set http proxy
            if (proxy != null) {
                String errMsg = MessageHelper.getMessage("cli.errproxy", proxy);
                String[] pcs = proxy.split(":");
                if (pcs == null || pcs.length != 2) {
                    System.out.println(errMsg);
                    return;
                }
                try {
                    HttpCaller.setProxyHost(pcs[0], Integer.parseInt(pcs[1]), null);
                } catch (Exception e) {
                    System.out.println(errMsg);
                    return;
                }
            }

            HttpReturn ret = HttpCaller.invokeReturn(builder.build());

            if (curlOnly) {
                System.out.println("---- curlString = " + ret.getResponseStr());
            } else {
                if (isDebug) {
                    System.out.println("Diagnostic Info:" + ret.diagnosticInfo);
                }
                System.out.println("---- response http headers = " + ret.responseHeaders);
                if (changeCharset) {
                    System.out.println("\n---- retStr after changeCharset = " + HttpCaller.changeCharset(ret.getResponseStr()));
                } else {
                    System.out.println("---- retStr = " + ret.getResponseStr());
                }

                //call multi-times for stress or flow-ctrl testing
                int times = Integer.getInteger("test.stress.times", 0);
                for (int i = 2; i <= times; i++) {
                    ret = HttpCaller.invokeReturn(builder.build());
                    System.out.println("---- retStr [#" + i + "] = " + ret.getResponseStr());
                }
            }
        } catch (Exception e) {
            System.out.println("-- operation error：" + e.getMessage());
            //if (isDebug)
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
            System.out.println("Bad param: " + message);

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar http-client.jar [options...]", opt);
        System.out.println("\ncurrent SDK version:" + SDK_VERSION + "\n----");
        System.out.println("\nCurrent JDK Env: file.encoding=" + System.getProperty("file.encoding") + "");
        Version.version();
    }

}
