# 签名机制
参见[http-client签名说明](https://github.com/aliyun/csb-sdk/tree/master/http-client#%E7%AD%BE%E5%90%8D%E6%9C%BA%E5%88%B6%E7%9A%84%E8%AF%B4%E6%98%8E)

# Java签名串生成方法
参见 [CsbSignature源代码及说明](src/main/java/com/alibaba/csb/CsbSignature.java)。

# http请求示例
假设当前要访问csb的信息如下：
* csb服务名：http2http1
* csb服务版本号：1.0.0
* csb服务访问地址： http://csb.broker.com:8086/CSB
* csb凭证ak： ak123
* csb凭证sk： sk456
* 当前时间戳： 1592225468715
* 请求参数1: name=中文name1
* 请求参数2：times=123

## form表单提交的http消息
```http request
http://csb.broker.com:8086/CSB

_api_timestamp:1592225468715
_api_name:http2http11
_api_signature:签名值
_api_version:1.0.0
_api_access_key: ak123
Content-Type:application/x-www-form-urlencoded; charset=UTF-8

name=%E4%B8%AD%E6%96%87name1&times=123
```

## json或xml提交的http消息
```http request
http://csb.broker.com:8086/CSB

_api_timestamp:1592225468715
_api_name:http2http11
_api_signature:签名值
_api_version:1.0.0
_api_access_key:ak123
Content-Type:application/json; charset=UTF-8

{"name":"中文name1", "time1":123}
```
