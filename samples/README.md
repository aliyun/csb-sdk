SDK工具使用的参考例子

A. 前提需求：

1. 请先下载本例子所需要的两个SDK JAR文件到 ./lib 目录下
wget http://middleware-udp.oss-cn-beijing.aliyuncs.com/components/csb/CSB-SDK/http-sdk-1.0.4.2plus.jar  另存为http-sdk.jar
wget http://middleware-udp.oss-cn-beijing.aliyuncs.com/components/csb/CSB-SDK/ws-sdk-1.0.4.2plus.jar    另存为ws-sdk.jar

如果在unix相关环境下，可以在lib/目录下执行 . get-jars.sh

2. 确保你的broker服务能够提供PING测试服务， 它是broker自带的二个测试服务，服务名都是:PING 服务版本不同: vcsb 和 vcsb.ws, 它可以提供四个开放服务：
   a. http服务       http://broker-vip:8086/CSB  PING vcsb             (broker内置的restful 测试服务)
   b. webservice服务 http://broker-vip:9081/PING/vcsb/ws2restful?wsdl  （restful发布成ws）
   c. http服务       http://broker-vip:8086/CSB  PING vcsb.ws          (ws发布成restful)
   d. webservice服务 http://broker-vip:9081/PING/vcsb.ws/ws2ws?wsdl     (ws发布成ws 透传)

3. 在pom.xml里使用cxf plugin通过WSDL生成client端的jax-ws文件,这些生成的java源文件会参与本测试的编译和运行（mvn generate-sources）

B. 测试代码
1. ./src/test/java/com/alibaba/csb/sdk/http/HttpSDKTest.java # 如何使用HTTP SDK的用例

2. ./src/test/java/com/alibaba/csb/sdk/ws/WSSDKTest.java  # 如何使用WS SDK的用例

C. 调用 (注意使用-Dbhost= 指向你的broker-vip)
   mvn test -Dbhost=broker-vip


D. WS2WS + MTOM测试Case

1. ./src/test/java/com/alibaba/csb/sdk/ws/MtomSDKTest.java #测试调用MTOM方式的ws2ws的透传调用
2. client端的生成类资源是由 wsdl: ./src/main/resources/mtom.wsdl 生成， 参见pom.xml中的cxf-codegen-plugin部分
注意： MTOM测试Case需要预先运行MTOM服务接入端服务，并在CSB中发布后才能进行测试运行