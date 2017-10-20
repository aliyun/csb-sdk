# CSB-SDK README

## Introduction

The CSB-SDK is a client-side invocation SDK for HTTP or Web Service API opened by the CSB (Cloud Service Bus) product. It is responsible for invoking the open API and signing the request information.

## Content in the CSB-SDK
* common    base classes used by both HTTP-SDK and WS-SDK 
* HTTP-SDK  The standalone client SDK for invoking HTTP API  [details](http-client/README.md)
* WS-SDK    The client SDK for binding security params into WebService client dispatch or port [details](ws-client/README.md)
* Samples   Unit Tests for using above SDKs [details](samples/README.md)

## RELEASE
* Build as standalone client jar:

```
# Build http SDK
cd http-client
bash gen-standaloneJar.sh

# Build WS SDK
cd ws-client
bash gen-standaloneJar.sh

```

# Use the SDK as dependency in your pom.xml
1. Directly use the maven dependency from maven central repository. Use release dependency in your pom.xml

```
  <!--To use HTTP-SDK-->
  <dependency>
     <groupId>com.alibaba.csb.sdk</groupId>
     <artifactId>http-client</artifactId>
     <version>1.1.0</version>
  </dependency>

  <!--To use WS-SDK-->
  <dependency>
     <groupId>com.alibaba.csb.sdk</groupId>
     <artifactId>ws-client</artifactId>
     <version>1.1.0</version>
  </dependency>
```

2. Use snapshot dependency in your pom.xml

```
  <!--To use HTTP-SDK-->
  <dependency>
     <groupId>com.alibaba.csb.sdk</groupId>
     <artifactId>http-client</artifactId>
     <version>1.0.4.4-SNAPSHOT</version>
  </dependency>

  <!--To use WS-SDK-->
  <dependency>
     <groupId>com.alibaba.csb.sdk</groupId>
     <artifactId>ws-client</artifactId>
     <version>1.0.4.4-SNAPSHOT</version>
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

* Release Notes
  [change history](release.md)

## Other-Languages support (beta version!!!)
1. PHP based HTTP-SDK
A http-sdk implemente by PHP script [details](others/php/README.md)

2. GO  based HTTP-SDK
A http-sdk implemente by GO script [details](others/golang/README.md)

3. node.js based HTTP-SDK
A http-sdk implemente by node.js [details](others/node.js/README.md)
## Future Work

- Support more Aliyun base service
- Support more friendly code migration.

## License

Licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)