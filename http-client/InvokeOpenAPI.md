如下是使用http-sdk对CSB-Console提供的openAPI的调用的例子
----

```
package com.alibaba.openapi;

//如下的类 来自http-sdk
import com.alibaba.csb.sdk.CmdCaller;
import com.alibaba.csb.sdk.HttpCaller;
import com.alibaba.csb.sdk.HttpCallerException;
import com.alibaba.csb.sdk.HttpParameters;

//如下的类来自csb-console
//import com.alibaba.osp.console.api.response.Result;
//import com.alibaba.osp.console.domain.SimpleCsbInstance;

import org.junit.Test;
import com.alibaba.fastjson.JSON;
import java.util.List;

/**
 * 单元测试, 使用HttpCaller进行Console OpenAPI的调用
 *
 * 注意: 本SDK只能访问私有部署的CSB-Console, 不能访问公有云的CSB-Console. 安全限制的原因。
 * 建议使用POP API方式调用公有云控制台。
 * <pre>
 *   首先在你的pom.xml中加入http-sdk依赖包
 *   <dependency>
 *     <groupId>com.alibaba.csb.sdk</groupId>
 *     <artifactId>http-client</artifactId>
 *     <version>1.1.0</version>
 *   </dependency>
 * </pre>
 *
 * <p>
 * Created by wiseking on 2017/9/4.
 */
public class HttpCallerOpenAPITest {
  public static final String reqUrl = "http://lcsb.daily.taobao.net:8080";
  public static final String apiListCsb = "/api/csbinstance/listCsbs";
  public static final String apiAddPrj = "/api/project/createorupdate";

  //实例或用户凭证对应的ak,sk
  public static final String ak = "xxx";
  public static final String sk = "xxx=";


  /**
   * 测试 使用sdk命令行方式调用open API
   */
  @Test
  public void testMainCmd() {
    //List CSB GET
    //  命令行调用获取实例列表
    // java -jar http-sdk.jar get "http://lcsb.daily.taobao.net:8080/api/csbinstance/listCsbs" "/api/csbinstance/listCsbs" 1.1.0.0 ak sk
    CmdCaller.main(new String[]{"get", reqUrl + apiListCsb, apiListCsb, "1.0.0", ak, sk});

    //  命令行调用在某实例上增加一个服务分组
    // java -Ddfile=dfile.prop -jar http-sdk.jar post "http://lcsb.daily.taobao.net:8080/api/project/createorupdate?csbId=175" "/api/project/createorupdate" 1.1.0.0 ak sk
    System.setProperty("dfile", "./src/test/resources/dfile.prop");
    CmdCaller.main(new String[]{"post", reqUrl + apiAddPrj + "?csbId=175", apiAddPrj, "1.0.0", ak, sk});
    System.clearProperty("dfile");
  }


  /**
   * 测试 使用sdk编程方式调用open API
   */
  @Test
  public void testListCsb() {
    try {
      // 使用HttpCaller调用Console Open API的例子
      // 1. 获取实例列表

      // 设置请求参数
      HttpParameters.Builder hp = HttpParameters.newBuilder();
      hp.requestURL(reqUrl + apiListCsb);    //请求的URL需要是访问API的完整URL
      hp.api(apiListCsb).version("1.1.0.0"); //设置请求的api和版本, API需要以"/"开头
      hp.accessKey(ak).secretKey(sk);        //设置ak,sk 使用当前操作的实例的ak,sk
      hp.method("get");                      //设置调用方式

      String ret = null;
      ret = HttpCaller.invoke(hp.build());          //进行调用
      System.out.println("retString = " + ret);     //处理后续返回结果
      System.out.println();
      if (ret != null) {
        //转换成具体的对象
        //Result result = JSON.parseObject(ret, Result.class);
        //List<SimpleCsbInstance> instances = result.getArray("items", SimpleCsbInstance.class);
        //System.out.println("instances = " + instances);
      }
    } catch (HttpCallerException e) {
      e.printStackTrace();
    }
  }


  /**
   * 测试 使用sdk编程方式调用open API
   */
  @Test
  public void testAddProject() {
    try {
      String data = "{\"description\":\"openapi test\",\"projectName\":\"lt-wiseking\"}";

      // 使用HttpCaller调用Console Open API的例子
      // 2. 增加一个服务分组  (POST方式调用)

      // 设置请求参数
      HttpParameters.Builder hp = HttpParameters.newBuilder();
      hp.requestURL(reqUrl + apiAddPrj + "?csbId=176"); // 请求地址可以附带请求参数
      hp.api(apiAddPrj).version("1.1.0.0");             // 设置调用的api和版本
      hp.accessKey(ak).secretKey(sk);                   // 设置所属实例的ak,sk
      hp.method("post");                                // 设置调用方式
      hp.putParamsMap("data", data);                    // 设置请求的form body参数

      String ret = HttpCaller.invoke(hp.build());       // 进行调用
      System.out.println("retStr =" + ret);
      // 可以将它转换为Reult对象,进行后续处理
    } catch (HttpCallerException e) {
      e.printStackTrace();
    }
  }
}
```
