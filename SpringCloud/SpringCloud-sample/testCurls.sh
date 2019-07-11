curl  -v  'localhost:9000/checkhelth'
curl  -v  'localhost:9000/broker/nginxStatus'

curl  -v  'localhost:9000/broke/resttemplate/get/http2http1?name=sufan&times=1'
curl  -v  'localhost:9000/broke/resttemplate/post/http2http1?name=sufan&times=1'

curl  -v  'localhost:9000/broker/feign/get/http2http1?name=sufan&times=1'
curl  -v  'localhost:9000/broker/feign/get/http2hsf1?name=sufan&times=1'
curl  -v  'localhost:9000/broker/feign/post/http2http1?name=sufan&times=1'

