# CSB-SDK 说明文档

## [README of English](https://github.com/aliyun/csb-sdk/blob/master/README.md)

## 介绍

提供了一套客户调用端集成开发工具，方便用户统一调用服务总线开放出来的服务。

## CSB-SDK的目录结构
* common    公共底层类,供HTTP-SDK和WS-SDK使用
* HTTP-SDK  调用HTTP服务的客户端SDK  [details](http-client/README.md)
* WS-SDK    调用WebService服务的客户端SDK [details](ws-client/README.md)
* Samples   使用上述两个SDK的单元测试示例 [details](samples/README.md)
* others    其他语言的SDK实现代码, 包括PHP, Go和Node.js

# 使用SDK

## 作为Maven依赖在pom.xml中引入

1. 引用stable版本 (注意version版本, 参考[release](release.md)的说明)
```
  <!--To use HTTP-SDK-->
  <dependency>
     <groupId>com.alibaba.csb.sdk</groupId>
     <artifactId>http-client</artifactId>
     <version>${http.sdk.version}</version>
  </dependency>

  <!--To use WS-SDK-->
  <dependency>
     <groupId>com.alibaba.csb.sdk</groupId>
     <artifactId>ws-client</artifactId>
     <version>${ws.sdk.version}</version>
  </dependency>
```

2. 使用snapshot依赖

```
  <!--To use HTTP-SDK-->
  <dependency>
     <groupId>com.alibaba.csb.sdk</groupId>
     <artifactId>http-client</artifactId>
     <version>1.1.0-SNAPSHOT</version>
  </dependency>

  <!--To use WS-SDK-->
  <dependency>
     <groupId>com.alibaba.csb.sdk</groupId>
     <artifactId>ws-client</artifactId>
     <version>1.1.0-SNAPSHOT</version>
  </dependency>
     
     
  <repositories>
    ...
      
    <!--define snapshot repository for SDK releases-->   
    <repository>
      <id>csb-sdk-snapshots</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
      <releases>
        <enabled>false</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
```

## 使用独立的客户包

下面的命令可以把SDK代码及其依赖的底层JAR包统一打包成一个包,客户直接在其CLASSPATH中应用这个唯一的包即可执行SDK的调用功能

```
# Build http SDK
cd http-client
bash gen-standaloneJar.sh

# Build WS SDK
cd ws-client
bash gen-standaloneJar.sh

```

# 发布版本说明
  [修改历史](release.md)

# 其它语言的SDK (beta version!!!)
注意: 目前其他语言只对HTTP-SDK进行支持, WS-SDK的调用只提供Java语言版本

1. PHP 版本的HTTP-SDK [details](others/php/README.md)

2. Go 版本的HTTP-SDK  [details](others/golang/README.md)

3. node.js 版本的HTTP-SDK [details](others/node.js/README.md)

## 未来计划

- 支持更多的Aliyun基础服务
- 支持高可用和异步的客户端

## License

使用的License [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)