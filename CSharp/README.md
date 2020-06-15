# C#签名串生成方法
参见 csbSignature 源代码及说明。

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
http://11.162.130.197:8086/CSB

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
http://11.162.130.197:8086/CSB

_api_timestamp:1592225468715
_api_name:http2http11
_api_signature:签名值
Accept-Encoding:gzip
_api_version:1.0.0
_api_access_key:ak123
Content-Type:application/json; charset=UTF-8

{"name":"中文name1", "time1":123}
```