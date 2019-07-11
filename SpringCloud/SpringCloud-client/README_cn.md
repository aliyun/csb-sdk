# CSB SpringCloud客户端文档

SpringCloud环境下,CSB提供了Feign和RestTemplate的插件,方便开发者调用CSB上的服务
## 使用方式

### 1. 配置ak,sk
```
csb.ak=xxx-ak
csb.sk=yyy-sk
```
订阅服务时使用的ak,sk

### 2. 配置客户端interceptor
#### Feign
```java
com.alibaba.csb.springcloud.interceptor.CsbFeignRequestInterceptor
```
Feign不需要额外配置,默认会加载csb的interceptor

#### RestTemplate
```Java
@Component
public class CsbRestTemplateConfigration {
    @Value("${csb.ak}")
    private String ak;
    @Value("${csb.sk}")
    private String sk;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        CsbRestTemplateInterceptor csbInterceptor = new CsbRestTemplateInterceptor();
        csbInterceptor.setAk(this.ak);
        csbInterceptor.setSk(this.sk);


        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(csbInterceptor);

        return restTemplate;
    }
}
```

### 3. 配置服务
#### Feign
```Java
@FeignClient(value = "csb-aliyun-cn-shanghai-sufan",configuration = CsbFeignConfiguration.class)
public interface CsbFeignService {
    @RequestLine("GET /csb?name={name}&times={times}")
    @Headers({"_api_name: http2http1", "_api_version: 1.0.0"})
    String csbGetHttp2http1(@Param("name") String name, @Param("times") String times);
    @RequestLine("POST /csb")
    @Headers({"_api_name: http2http1", "_api_version: 1.0.0","Content-Type: application/json;charset=UTF-8"})
    String csbPostHttp2http1(UserInfo userInfo);

    @RequestLine("GET /nginx_status")
    String csbNginxStatus();
}
```

#### RestTemplate
```Java
@Component
public class CsbRestTemplateService {
    @Value("${csb.broker.url}")
    private String csbURL;
    @Autowired
    private RestTemplate restTemplate;

    public String csbGetHttp2http1(String name, String times) {

        MultiValueMap<String, String> data = new LinkedMultiValueMap();
        data.add("name", name);
        data.add("times", times);


        HttpEntity<MultiValueMap<String, String>> request = this.createRequest(
                "http2http1","1.0.0",
                null,
                data);
        String result =restTemplate.exchange(this.csbURL+"?name={name}&times={times}", HttpMethod.GET,request,String.class,data).getBody();

        return result;
    }
    public String csbPostHttp2http1(String name, String times) {

        MultiValueMap<String, String> data = new LinkedMultiValueMap();
        data.add("name", name);
        data.add("times", times);


        HttpEntity<MultiValueMap<String, String>> request = this.createRequest(
                "http2http1","1.0.0",
                MediaType.APPLICATION_FORM_URLENCODED,
                data);
        return restTemplate.postForObject(this.csbURL, request, String.class);
    }
}
```

## 示例
完整例子,可参考 SpringCloud-sample模块
## License

使用的License [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)