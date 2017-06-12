# WS-SDK README

## 1. WS-SDK下载地址


根据需要将该运行包放在调用端的CLASSPATH环境里
[ws-sdk-1.0.4.2plus.jar](http://middleware-udp.oss-cn-beijing.aliyuncs.com/components/csb/CSB-SDK/ws-sdk-1.0.4.2plus.jar)

## 2. WS-SDK功能

当WebService服务由CSB开放出来后，客户端需要生成标准的Proxy或Dispatch来进行调用, SDK的作用是每次调用时做方法拦截把安全需要的KV信息添加到HTTP请求头部分。
程序方式使用SDK编写的方式为：

```
import com.alibaba.csb.sdk.HttpCaller;
import com.alibaba.csb.ws.sdk.WSClientSDK;
...
```
**注意：**在编程方式调用时，首先要在整个JVM范围内启动一次WSClientSDK.warmup()来加载SDK所需要的类,
  否则在第一次调用WSClientSDK时会很慢(~5s)
  
```
//首先使用标准的WS	Client方法获取Proxy或者Dispath
MyPort		proxy	=	...;	
//bind	AK/SK到 proxy上
String	ak	=	"xxxxx";
String	sk	=	"xxxxx";
String	apiName	=	xx;
String	apiVersion	=	xx;
proxy	=	WSClientSDK.bind(proxy,	ak,	sk,	apiName, apiVersion);
//使用返回的Proxy，调用客户端方法
Response	response	=	proxy.method1(...);	
…
```

## 3. WSDL的开放说明

根据CSB的设计约定，当CSB开放成WebService服务时，对应的WSDL的地址为如下格式：

a. 如果接入是 HTTP 协议，则开放出的 WSDL 地址是：
```
http://broker-vip:9081/$api_name/$api_version/ws2restful?wsdl
```
  $api_name 为发布的服务名；
  $api_version 为发布的服务版本；
  "ws2restful"为固定值。  
b. 如果接入是 HSF 协议， 则开放出来的 WSDL 地址是：
```
http://broker-vip:9081/$api_name/$api_version/$method?wsdl
```
  $api_name 为发布的服务名；
  $api_version 为发布的服务版本；
  $method 为发布服务时对应的接入方法名。
c. 如果接入是 WS 协议（即WS透传）， 则开放出来的 WSDL 地址是：
```
http://broker-vip:9081/$api_name/$api_version/ws2ws?wsdl
```
  $api_name 为发布的服务名；
  $api_version 为发布的服务版本；
  "ws2ws"为固定值。


## 4.命令方式使用WS-SDK

为了快速测试一个CSB开放出来的WebService服务，WS-SDK工具包提供了命令行方式调用的工具。

### 4.1 命令行工具使用说明

```
$ java -jar ws-client.jar -h
usage: java -jar wsclient.jar [options...]
 -ak <arg>                    accessKey
 -api <arg>                   服务名
 -d,--debug                   打印调试信息
 -ea <arg>                    endpoint地址，e.g:
                              http://broker-ip:9081/api/version/method
 -h,--help                    打印帮助信息
 -ns <arg>                    在wsdl中定义的服务的target namespace
 -pname,--portName <arg>      在wsdl中定义的端口名
 -rd <arg>                    soap请求内容，如果设置该选项时，-rf选项被忽略
 -rf <arg>                    soap请求文件, 文件里定义soap请求的XML内容
 -sk <arg>                    secretKey
 -sname,--serviceName <arg>   在wsdl中定义的服务名
 -soap12                      -soap12 为soap12调用, 不定义为soap11
 -version <arg>               服务版本
 -wa <arg>                    wsdl地址，e.g:
                              http://broker-ip:9081/api/version/method?wsdl
```

TIP: 如何从已知的WSDL中确定上述调用参数

##### ![alt 从wsdl里取值](img/wsdl.png)

### 4.2 命令行使用例子

```
java -jar target/ws-client-1.0.4.4-SNAPSHOT.jar  -ak ak -sk sk -api PING -version vcsb \
  -wa http://11.239.187.178:9081/PING/vcsb/ws2restful?wsdl \
  -ea http://11.239.187.178:9081/PING/vcsb/ws2restful \
  -ns http://ws2restful.PING.csb/ -sname PING -pname ws2restfulPortType \
  -rd '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:test="http://ws2restful.PING.csb/">
<soapenv:Header/>
<soapenv:Body>
   <test:ws2restful>
      <name>abc</name>
   </test:ws2restful>
</soapenv:Body>
</soapenv:Envelope>'

```
如果不使用-rd选项，可以把请求内容保存到一个文件中，然后使用-rf 指定这个文件。

**注意：**命令行方式不支持调用附件或者MTOM形式的WSDL服务
