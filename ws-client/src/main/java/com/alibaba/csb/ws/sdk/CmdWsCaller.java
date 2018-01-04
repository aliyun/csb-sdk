package com.alibaba.csb.ws.sdk;

import java.io.File;
import java.io.IOException;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;

import com.alibaba.csb.sdk.CommUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

public class CmdWsCaller {
	private static final String SDK_VERSION = "1.0.4.2+";

	private static void usage(Options opt) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar wsclient.jar [options...]", opt);
		System.out.println("\ncurrent SDK version:" + SDK_VERSION+"\n----");
		try {
			System.out.println(CommUtil.geCurrenttVersionFile());
		} catch (IOException e) {
			//
		}
		System.exit(0);
	}

	private static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	private static void print(boolean debug, String format, Object args) {
		if (!debug) {
			return;
		}
		System.out.println(String.format(format, args));
	}

	/**
	 * 使用Dispatch方式发送soap请求调用WS
	 * 
	 * @param params
	 * @param ns
	 * @param sname
	 * @param pname
	 * @param isSoap12
	 * @param wa
	 * @param ea
	 * @param reqSoap
	 * @throws Exception
	 */
	private static void invokeWithDispath(WSParams params, String ns, String sname,
			String pname, boolean isSoap12, String wa, String ea, String reqSoap) throws Exception {
		Dispatch<SOAPMessage> dispatch = WSInvoker.createDispatch(params, ns, sname, pname, isSoap12, wa, ea);
		SOAPMessage request = WSInvoker.createSOAPMessage(isSoap12, reqSoap);
		SOAPMessage reply = dispatch.invoke(request);

		String response = DumpSoapUtil.dumpSoapMessage(reply);

		if (response != null)
			System.out.println("\n-- 调用返回:\n" + response);
		else
			System.out.println("\n-- 调用返回为空");
		
		//call multi-times for stress or flow-ctrl testing
		int times = Integer.getInteger("test.stress.times",0);
		for(int i=2; i<=times; i++) {
			reply = dispatch.invoke(request);;
			System.out.println("---- [#"+i+"] times call it for stress testing");
			if (reply != null)
				System.out.println("\n-- 调用返回:\n" + DumpSoapUtil.dumpSoapMessage(reply));
			else
				System.out.println("\n-- 调用返回为空");
		}
	}

	public static void main(String[] args) {
		Options opt = new Options();
		opt.addOption("ak", true, "accessKey, 可选");
		opt.addOption("sk", true, "secretKey, 可选");
		opt.addOption("api", true, "服务名");
		opt.addOption("version", true, "服务版本");
		opt.addOption("wa", true, "wsdl地址，e.g: http://broker-ip:9081/api/version/method?wsdl");
		opt.addOption("ea", true, "endpoint地址，e.g: http://broker-ip:9081/api/version/method");
		opt.addOption("ns", true, "在wsdl中定义的服务的target namespace");
		opt.addOption("sname", "serviceName", true, "在wsdl中定义的服务名");
		opt.addOption("pname", "portName", true, "在wsdl中定义的端口名");
		opt.addOption("soap12", false, "-soap12 为soap12调用, 不定义为soap11");
		opt.addOption("nonce", false, "-nonce 是否做nonce防重放处理，不定义为不做nonce重放处理");
		opt.addOption("h", "help", false, "打印帮助信息");
		opt.addOption("d", "debug", false, "打印调试信息");
		opt.addOption("rf", true, "soap请求文件，文件里存储soap请求的Message格式内容");
		opt.addOption("rd", true, "soap请求内容(Message)，如果设置该选项时，-rf选项被忽略");

		CommandLineParser parser = new DefaultParser();

		Boolean isDebug = false;
		try {
			CommandLine commandline = parser.parse(opt, args);
			if (commandline.hasOption("h")) {
				usage(opt);
				return;
			}

			String ak = commandline.getOptionValue("ak");
			String sk = commandline.getOptionValue("sk");
			String api = commandline.getOptionValue("api");
			String version = commandline.getOptionValue("version");
			String wa = commandline.getOptionValue("wa");
			String ea = commandline.getOptionValue("ea");
			String ns = commandline.getOptionValue("ns");
			String sname = commandline.getOptionValue("sname");
			String pname = commandline.getOptionValue("pname");
			String rf = commandline.getOptionValue("rf");
			String rd = commandline.getOptionValue("rd");
			boolean isSoap12 = commandline.hasOption("soap12");
			boolean nonce = commandline.hasOption("nonce");
			isDebug = commandline.hasOption("d");

			if (isDebug) {
				// printParams();
				System.out.println("ak=" + ak);
				System.out.println("sk=" + sk);
				System.out.println("api=" + api);
				System.out.println("version=" + version);
				System.out.println("isSoap12=" + isSoap12);
				System.out.println("nonce=" + nonce);
				System.out.println("wa=" + wa);
				System.out.println("ea=" + ea);
				System.out.println("ns=" + ns);
				System.out.println("sname=" + sname);
				System.out.println("pname=" + pname);
				System.out.println("rd=" + rd);
				if (isEmpty(rd)) {
					System.out.println("rf=" + rf);
				}
			}

			if (isEmpty(api) || isEmpty(version) || isEmpty(ea) || isEmpty(wa) || isEmpty(ns) || isEmpty(sname)
					|| isEmpty(pname) || (isEmpty(rf) && isEmpty(rd))) {
				usage(opt);
				return;
			}

			String reqData = (isEmpty(rd)) ? FileUtils.readFileToString(new File(rf)) : rd;
			print(isDebug, "-- 请求报文: \n%s\n", reqData);
			if (isEmpty(reqData)) {
				print(true, "-- 操作失败：文件%s请求报文为空", rf);
				return;
			}
			WSParams params = WSParams.create().accessKey(ak).secretKey(sk).api(api).version(version).nonce(nonce);
			invokeWithDispath(params, ns, sname, pname, isSoap12, wa, ea, reqData);
		} catch (Exception e) {
			System.out.println("-- 操作失败：" + e.getMessage());
			if (isDebug)
				e.printStackTrace(System.out);
		}
	}
}
