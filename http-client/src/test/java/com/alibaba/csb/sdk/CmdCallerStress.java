package com.alibaba.csb.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用java命令行的方式调用CSB开放出来的Http服务，这种方式通常用来快速测试服务和参数是否正确
 * 
 *  java -jar httpclient.jar 会打印具体的操作用法
 * 
 * @author Alibaba Middleware CSB Team
 * @since 2016
 * 
 */
public class CmdCallerStress {
	private static void usage() {
		System.out.println("Usage: java -jar Http-client.jar method url apiName version [ak sk]");
	}
	public static void main(String args[]) throws Exception {
		//System.out.println("args.length="+args.length);
		if (args.length != 6 && args.length != 8) {
			usage();
			return;
		}
		String method = args[0];
		if (!"get".equalsIgnoreCase(method) && !"post".equalsIgnoreCase(method)) {
			usage();
			return;
		}
		
		String lurl = args[1];
		String apiName = args[2];
		String version = args[3];
		
		if("null".equalsIgnoreCase(args[3]))
			version = null;
		
		String ak = null;
		String sk = null;

		System.out.println("---- restful request url:" + lurl);
		System.out.println("---- apiName:" + apiName);
		System.out.println("---- version:" + version);
		
		if (args.length == 8) {
			ak = args[6];
		    sk = args[7];
		
		   if ("".equals(ak))
			 ak = null;
		   System.out.println("---- ak:" + ak);
		   System.out.println("---- sk:" + sk);
		}
		Map<String, String> params = new HashMap<String, String>();

		int concurrencyCount = Integer.parseInt(args[4]);
		int requestCount = Integer.parseInt(args[5]);

		final CountDownLatch countDownLatch = new CountDownLatch(concurrencyCount * requestCount);
		ExecutorService executor = Executors.newFixedThreadPool(concurrencyCount);
		for (int t = 0; t < concurrencyCount; ++t) {
			executor.submit(createJob(countDownLatch, requestCount, method, lurl, apiName, version, params, ak, sk));
		}

		countDownLatch.await();
		
		System.out.println("******************** stress end *********************");

		// runTestJson();
	}
	
	private static Runnable createJob(final CountDownLatch countDownLatch,final int requestCount,final String method, final String lurl, final String apiName, final String version, final Map<String, String> params, final String ak, final String sk) {
		return new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < requestCount; ++i) {
					try {

						String ret = null;
						if ("get".equalsIgnoreCase(method))
							ret = HttpCaller.doGet(lurl, apiName, version, params, ak, sk);
						else
							ret = HttpCaller.doPost(lurl, apiName, version, params, ak, sk);
						
						System.out.println("---- retStr = " + ret);
						System.out.println("---- retStr after changeCharset = " + HttpCaller.changeCharset(ret));
					} catch (Throwable e) {
						e.printStackTrace();
					} finally {
						countDownLatch.countDown();
					}
				}
			}
		};
	}
}
