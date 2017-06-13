# HTTP SDK README

HTTP SDK工具类，用来向服务端发送HTTP请求，请求支持POST/GET方式。如果提供了AccessKey和SecurityKey参数信息，它能够在内部将请求消息进行签名处理，然后向CSB服务端发送进行验证和调用。

## 0. CSB发布出的Restful服务的访问地址
地址访问格式： http://broker-vip:8086/CSB
* broker-vip 是CSB-Broker的前置SLB地址或者具体的一个broker的地址(当没有前置的SLB或者Proxy)
* 默认的访问端口为 "8086"
* 请求的context-path可以任意指定，默认使用“CSB”
  
## 1. 工具包的下载地址

根据需要将该运行包放在调用端的CLASSPATH环境里
[http-sdk-1.0.4.2plus.jar](http://middleware-udp.oss-cn-beijing.aliyuncs.com/components/csb/CSB-SDK/http-sdk-1.0.4.2plus.jar)

## 2. HTTP Client SDK 使用方式

### 方式一: 使用命令行直接调用
这个方式适合开发测试使用，不需要编写代码，快速地查看一个服务是否可通可用。
```
java [-Dhfile=httpheaders.properties] -jar http-sdk.jar method url api version [ak sk]
```
参数取值说明:
* **method**  调用Restful服务的方式，目前可以取值为：POST,GET,cpost,cget
   POST   以post形式调用服务
   GET    以get形式调用服务
   cpost或者cget  SDK不去真正的调用开放的服务而是生成一个curl string, 用户可以使用这个串直接调用执行curl命令，而不再需要SDK
* **url**        要调用的RESTful服务的URL,包括地址和调用参数
* **api**        CSB发布的RESTful服务的全名
* **version**    CSB发布的 RESTful服务的版本（当设置为“null”时为不设置version）
* **ak** **sk**  即accessKey和secretKey，必须同时提供，如果不需要安全认证，则不要输入,或者输入任意的串值，如: "ak" "sk"
* **-v**         打印当前的SDK版本
* **hfile**      可选的**JVM系统参数**，它定义一个属性文件定义要传递给服务端的http headers

-Dhfile 所指向的属性文件的为标准的属性文件格式如下：
```
#注解 设置我的header
header1=test1
header2=test2
```

### 方式二: 使用编程方式调用

```
 import com.alibaba.csb.sdk.HttpCaller;
 import com.alibaba.csb.sdk.HttpCallerException;
 ...
```
  **注意：**在编程方式调用时，首先要在整个JVM范围内启动一次HttpCaller.warmup()来加载SDK所需要的类,
  否则在第一次调用HttpCaller的doGet/doPost/invoke等方法时会很慢(~5s)

```
  HttpCaller.warmup();
```
 
  (1) 使用Builder的方式构造调用参数，然后进行调用 （推荐用法）
```  
 import com.alibaba.csb.sdk.HttpParameters;
 import com.alibaba.csb.sdk.HttpCaller;
 import com.alibaba.csb.sdk.HttpCallerException;
 
  
  HttpParameters.Builder builder = HttpParameters.newBuilder();
      
  builder.requestURL("http://broker-vip:8086/CSB?arg0=123") // 设置请求的URL
      .api("test") // 设置服务名
      .version("1.0.0") // 设置版本号
      .method("get") // 设置调用方式, get/post
      .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey
      
   // 设置请求参数
   builder.putParamsMap("key1", "value1");
   builder.putParamsMap("key2", "{\"a\":value1}"); // json format value
      
   //设置请求调用方式
   builder.method("get");
      
   //设置透传的HTTP Headers
   builder.putHeaderParamsMap("header1", "value1");
   builder.putHeaderParamsMap("header2", "value2");
      
   //进行调用 返回结果
   String result = null;
   try {
      	result = HttpCaller.invoke(builder.build());
        //如果期望获取返回的http headers, 则需要在invoke中加入第二个参数，如下：
        StringBuffer resHttpHeaders = new StringBuffer();
        result = HttpCaller.invoke(builder.build(), resHttpHeaders);
      
        //注：如果返回结果出现乱码(不能正常显示中文),可以使用串字符集转换方法进行转换
        result = HttpCaller.changeCharset(result);
   } catch (HttpCallerException e) {
      	// error process
   }
      
   try {
      	// 重启设置请求参数
      	builder.clearParamsMap();
      	builder.putParamsMap("key1", "value1---new");
      	builder.putParamsMap("key2", "{\"a\":\"value1-new\"}");
      
      	// 使用post方式调用
      	builder.method("post");
      	result = HttpCaller.invoke(builder.build());
   } catch (HttpCallerException e) {
      	// error process
   }
```      
 (2) 如果使用json或者bytes内容的作为http body，使用下面的方法
```
  //构造ContentBody对象
  ContentBody cb = new ContentBody(jsonObject.toSring());
  //或者
  cb = new ContentBody(file2bytes);
     
  //ContentBody传递，要求使用post方式进行调用
  //如果需要传递请求参数 可以拼接到请求URL中，或者设置paramsMap参数由SDK内部进行拼接
  HttpParameters.Builder builder = HttpParameters.newBuilder();      
  builder.requestURL("http://broker-vip:8086/CSB?arg0=123") // 设置请求的URL,可以拼接URL请求参数
      .api("test") // 设置服务名
      .version("1.0.0") // 设置版本号
      .method("post") // 设置调用方式, 必须为 post
      .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey
     
  builder.contentBody(cb);
      
  //进行调用，返回结果
  String result = null;
  try {
      	result = HttpCaller.invoke(builder.build());
  } catch (HttpCallerException e) {
      	// error process
  }     
```     
 (3) 直接调用方式 (旧的使用方式，已过期，不推荐)
```
 Map<String,String> params = new HashMap<String,String>();
    
 Object smd = ... // 一个具体的复杂对象
 if (smd != null) {
   String data = JSON.toJSONString(smd); //转换为JSON String
   params.put("data", data);
 }
 
 // -- Tip: 如果调用者无法获得复杂对象参数类，则可以使用全map的方式设置json串，举例，对于json串
 // {"f1":{"f11":"v11", "f12":["v121","v122"]}, "f2":"wiseking"}
 // 它是可以通过如下的方式进行转换而来
 Map map = new HashMap();
 
 Map mapF1 = new HashMap();
 mapF1.put("f11", "v11");
 mapF1.put("f12", Arrays.asList("v121","v122"));
 map.put("f1", mapF1);
 
 map.put("f2", "wiseking");
 String jsonData = JSON.toJSONString(map);
 // -- Tip End
 
 params.put("name", "abcd"); //普通的串对象
 params.put("password", "abcd"); //普通的串对象
 
    
 String requestURL = "http://broker-vip:8086/CSB";
 String API_NAME = "login_system";
 String version = "1.0.0";
 String ak = "xxxxxx";
 String sk = "xxxxxx"; //用户安全校验的签名密钥对
    
 try {
   String result = HttpCaller.doPost(requestURL, API_NAME, version, params, ak, sk);
    
   if (result != null) {
      //返回结果处理, 如转换为JSON对象
      ...
   }
 } catch (HttpCallerException ie) {
      //print error
 }}
 
```

## 4. 附录 
4.1. 高级功能 关于连接参数的设置：
   a. 可以为http/https设置以下的全局性系统参数： 
      -Dhttp.caller.connection.max          设置连接池的最大连接数，默认是20
      -Dhttp.caller.connection.timeout      设置连接超时时间（毫秒），默认是-1， 永不超时
      -Dhttp.caller.connection.so.timeout   设置读取超时时间（毫秒），默认是-1， 永不超时
      -Dhttp.caller.connection.cr.timeout   设置从连接池获取连接实例的超时（毫秒），默认是-1， 永不超时      
      -Dhttp.caller.skip.connection.pool    如何设置为true,则不使用连接池。默认行为是false,使用连接池(支持长连接)
      -Dhttp.caller.connection.async        设置内部使用nio,默认fasle:同步io,true:nio（不支持连接池，不推荐使用）
   b. 也可以使用下面的方法设置以上的某一个或者多个参数：
      Map sysParams = new HashMap();
      sysParams.put("http.caller.connection.timeout","3000"); //设置连接超时未3秒
      HttpCaller.setConnectionParams(sysParams); //注意：本次设置只对本线程起作用
      ...
      HttpCaller.doPost() or doGet();

4.2. 在无Java对象的情况下，使用泛化的形式转换json串的工具
```
  一个辅助工具类Java对象到JSON串的泛化转换，在不定义复杂对象类的情况下，把HTTP参数转换为Json串

 用法：
 
 import static com.alibaba.csb.sdk.ParamJSONHelper.*;
 
  ...
  Map orderItemElement = toMap(
    toKVPair("skey1", "foo"), 
    toKVPair("skey2", "bar"));
  Map orderItem = toMap(toKVPair("orderItem", orderItemElement));
  Map itemElement = toMap(
    toKVPair("key1", "love"), 
    toKVPair("key2", "story"),
    toKVPair("orderItems", Arrays.asList(orderItem)));
  Map item = toMap(toKVPair("item", itemElement));
  // 调用fastjson类 将Map所代表的对象内容装换为json串
  String poItems = JSON.toJSONString(Arrays.asList(item), true);
      
  System.out.println("poItems=\n" + poItems);
 
 打印出的JSON串为:
 
 [
	{
		"item":{
			"key1":"love",
			"key2":"story",
			"orderItems":[
				{
					"orderItem":{
						"skey1":"foo",
						"skey2":"bar"
					}
				}
			]
		}
	}
 ]
```

4.3. 如何生成sdk jar包
```
1. mvn clean
2. . ver.sh
3. mvn assembly:assembly -Dmaven.test.skip
```
