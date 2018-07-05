# CSB-SDK 说明文档

## [README of English](https://github.com/aliyun/csb-sdk/blob/master/README.md)

## 介绍

提供了一套客户调用端集成开发工具，方便用户统一调用服务总线开放出来的服务。

## CSB-SDK的目录结构
* common       公共底层类,供HTTP-SDK和WS-SDK使用
* http-client  调用HTTP服务的客户端SDK  [details](http-client/README.md)
* ws-client    调用WebService服务的客户端SDK [details](ws-client/README.md)
* samples      使用上述两个SDK的单元测试示例 [details](samples/README.md)
* others       其他语言的SDK参考实现代码, 包括PHP, Go和Node.js 

注意: 我们只提供Java版本的SDK的维护和支持，对于其他语言的参考实现，暂时不提供维护和支持。

# 使用SDK

## 作为Maven依赖在pom.xml中引入

1. 引用stable版本 (注意具体的version版本, 参考[release](release.md)的说明)
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

2. 使用snapshot依赖 (注意具体snapshot的version版本, 参考[release](release.md)的说明)

```
  <!--To use HTTP-SDK-->
  <dependency>
     <groupId>com.alibaba.csb.sdk</groupId>
     <artifactId>http-client</artifactId>
     <version>${http.sdk.snapshot.version}</version>
  </dependency>

  <!--To use WS-SDK-->
  <dependency>
     <groupId>com.alibaba.csb.sdk</groupId>
     <artifactId>ws-client</artifactId>
     <version>${http.sdk.snapshot.version}</version>
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

## 从源码生成

注意：

-  本地应该提前安装Git, JAVA, MAVEN等工具,才可以从源码进行编译；
-  编译前，可以根据用户最终使用SDK的运行环境选择不同的JDK版本。

下面的命令会展示如何编译SDK源码，生成(1)maven依赖或者(2)独立JAR包。 所谓独立JAR包是SDK的编译class
及其依赖的jar包里的class文件重新打包成一个独立的JAR, 它可以运行命令工具(java -jar standalone.jar)，
还可以把这个jar包放置在用户客户端运行CLASSPATH就可以在运行时使用SDK编程接口。

```
# 1. download sourcefrom Github (once time only)
git clone https://github.com/aliyun/csb-sdk.git
cd csb-sdk
     

# 2. show and switch to desired branch, e.g.
git branch
git checkout 1.1.4.0

# 3. Build common module
cd common
mvn clean install 

# 4. Build http SDK
cd ../http-client

# 4.1 install as maven dependency to local repository
mvn clean install  -Dmaven.test.skip

# 4.2 build as a standalone jar
bash gen-standaloneJar.sh

# 5. Build WS SDK
cd ../ws-client

# 5.1 install as maven dependency to local repository
mvn clean install  -Dmaven.test.skip

# 5.2 build as a standalone jar
bash gen-standaloneJar.sh

```

# 发布版本说明
  [修改历史](release.md)

# 其它语言的SDK (beta version!!!)
注意: 目前其他语言只对HTTP-SDK进行支持, WS-SDK的调用只提供Java语言版本

1. PHP 版本的HTTP-SDK [details](others/php/README.md)

2. Go 版本的HTTP-SDK  [details](others/golang/README.md)

3. node.js 版本的HTTP-SDK [details](others/node.js/README.md)

4. .net 版本的 HTTP-SDK [details](https://github.com/neozhu/csb-sdk.net)

## 未来计划

- 支持更多的Aliyun基础服务
- 支持高可用和异步的客户端
- 完善的测试和分发

## License

使用的License [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)
