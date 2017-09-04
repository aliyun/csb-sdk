package com.alibaba.csb.sdk;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 使用java命令行的方式调用CSB开放出来的Http服务，这种方式通常用来快速测试服务和参数是否正确
 * 
 * java -jar httpclient.jar 会打印具体的操作用法
 * 
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq
 * 
 * @since 2016
 * 
 */
public class CmdCaller {
	private static final String SDK_VERSION = "1.0.4.2+";

	private static void usage() {
		System.out
				.println("Usage: java [-Dhfile=headers.prop] [-Ddfile=d.txt] -jar Http-client.jar method url apiName version [ak sk]");
		System.out.println("   method = get   :  call url with GET method ");
		System.out.println("   method = post  :  call url with POST method ");
		System.out.println("   method = cget  :  return curl request string with GET method,  ");
		System.out.println("   method = cpost :  return curl request string with POST method ");
		System.out.println("   system property 'hfile' is an optional，to set a file which defines http headers, its content format:");
		System.out.println("     header1=value1");
		System.out.println("     header2=value2");
		System.out.println("   system property 'dfile' is an optional，to set a file which defines body data, its content format:");
		System.out.println("     data1=value1");
		System.out.println("     data2=value2");
		System.out.println("     ");
		System.out.println("   print current SDK version: java -jar Http-client.jar -v ");
	}



	public static void main(String args[]) {
		try {
			if (args.length > 0 && "-v".equalsIgnoreCase(args[0])) {
				System.out.println("HttpCaller SDK version:" + SDK_VERSION);
				try {
					System.out.println(CommUtil.geCurrenttVersionFile());
				} catch (IOException e) {
					//e.printStackTrace();
				}
				return;
			}
			if (args.length != 4 && args.length != 6) {
				usage();
				return;
			}
			String method = args[0];
			if (!"get".equalsIgnoreCase(method) && !"post".equalsIgnoreCase(method)
					&& !"cget".equalsIgnoreCase(method) && !"cpost".equalsIgnoreCase(method)) {
				usage();
				return;
			}

			Properties headerProp = readPropFile("hfile");
			Properties dataProp = readPropFile("dfile");

			String lurl = args[1];
			String apiName = args[2];
			String version = args[3];

			if ("null".equalsIgnoreCase(args[3]))
				version = null;

			String ak = null;
			String sk = null;

			System.out.println("---- restful request url:" + lurl);
			System.out.println("---- apiName:" + apiName);
			System.out.println("---- version:" + version);
			System.out.println("---- method:" + method);

			if (args.length == 6) {
				ak = args[4];
				sk = args[5];

				if ("".equals(ak))
					ak = null;
				System.out.println("---- ak:" + ak);
				System.out.println("---- sk:" + sk);
			}
			String ret = null;

			boolean curlOnly = false;
			if (method.toLowerCase().startsWith("c")) {
				curlOnly = true;
				HttpCaller.setCurlResponse(true);
			}

			HttpParameters.Builder builder = HttpParameters.newBuilder();
			builder.api(apiName).version(version).method(method).requestURL(lurl).accessKey(ak).secretKey(sk);

			if (headerProp != null) {
				for (Entry<Object, Object> kv : headerProp.entrySet()) {
					System.out.println("---- put http header " + (String) kv.getKey() + ":" + (String) kv.getValue());
					builder.putHeaderParamsMap((String) kv.getKey(), (String) kv.getValue());
				}
			}
			if (dataProp != null) {
				for (Entry<Object, Object> kv : dataProp.entrySet()) {
					System.out.println("---- put data body " + (String) kv.getKey() + ":" + (String) kv.getValue());
					builder.putParamsMap((String) kv.getKey(), (String) kv.getValue());
				}
			}
			StringBuffer resHttpHeaders = new StringBuffer();
			ret = HttpCaller.invoke(builder.build(), resHttpHeaders);

			if (curlOnly) {
				System.out.println("---- curlString = " + ret);
			} else {
				System.out.println("---- response http headers = " + resHttpHeaders.toString());
				System.out.println("---- retStr = " + ret);
				System.out.println("\n---- retStr after changeCharset = " + HttpCaller.changeCharset(ret));
				
				//call multi-times for stress or flow-ctrl testing
				int times = Integer.getInteger("test.stress.times",0);
				for(int i=2; i<=times; i++) {
					ret = HttpCaller.invoke(builder.build(), resHttpHeaders);
					System.out.println("---- retStr [#"+i+"] = " + ret);
				}
			}
			
			System.out.println();
			//System.exit(0);
		} catch (HttpCallerException e) {
			System.out.println("---- sdk invoke error:" + e.getMessage());
		}
	}

	// TODO:move this method to common prj
	private static Properties loadProps(String propFile) throws IOException {
		Properties pro = new Properties();
		FileInputStream in = new FileInputStream(propFile);
		pro.load(in);
		in.close();

		return pro;
	}

	private static Properties readPropFile(String pfileKey) throws HttpCallerException {
		String pfile = System.getProperty(pfileKey);
		Properties headerProp = null;
		if (pfile != null) {
      System.out.println("---- pfile:" + pfile);
      try {
        headerProp = loadProps(pfile);
      } catch (IOException e) {
        throw new HttpCallerException("Failed to load file:" + pfile);
      }
    }
		return headerProp;
	}
}
