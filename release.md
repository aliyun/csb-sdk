## Latest version/最新的版本
* RELEASE
 1.  ${http.sdk.version} = 1.1.5.9
 2.  ${ws.sdk.version} = 1.1.5.9

## Maven项目依赖
 1. [http.sdk](https://mvnrepository.com/artifact/com.alibaba.csb.sdk/http-client)
 2. [ws.sdk](https://mvnrepository.com/artifact/com.alibaba.csb.sdk/ws-client)
 
## RELEASE History

| MajorV  | MinorV | Date(YYYYMMDD) | Changes                              | Details |
| ------- | ------ | -------------- | ------------------------------------ | ------- |
| 1.0.4.4 |        | 20170613       | Support return http response headers | [go](release/r20170613.md)|
| 1.0.4.4 |        | 20170719       | Support aixis call wrapper in ws-sdk | [go](release/r20170719.md)|
| 1.1.4   | snapshot |20180301     | support new CLI for http-sdk          | [go](release/r20180301.md)       |
| 1.1.4   | snapshot |20180321     | support proxy for http-sdk          |   [go](release/r20180321.md)     |
| 1.1.4   | snapshot |20180402     | support -cbJSON parameter for http-sdk  |  enable passing json body String in CLI  |
| 1.1.4.0 |        | 20180622      | support SignUtil SPI  <br> Support Diagnostic <br>| [go](release/r20180622.md) |
| 1.1.4.1 |        | 20180702      | | |
| 1.1.5.1 | snapshot | 20181031   | support self defined signImpl \/ verifySignImpl | [go](release/r20181031.md) |
| 1.1.5.2 | snapshot | 20190130   | fix ServiceLoader.iterator multithread bug | [go](release/r20181031.md) |
| 1.1.5.3 | snapshot | 20190603   | add trace function                         | [go](release/r20190603.md) |
| 1.1.5.4 | snapshot | 20190710   | add http file&gzip surpport                         | [go](release/r20190710.md) |
| 1.1.5.5 | snapshot | 20190810   | add http array surpport                         | [go](release/r20190810.md) |
| 1.1.5.6 | snapshot | 20190925   | 1. http-client connect default parameters.<br/>2. fix sdk log performance bug.              | [go](release/r20190925.md) |
| 1.1.5.7 | snapshot | 20191211   | 1.fix http2ws content-type resolve <br/>2. add soapaction option<br/>            | [go](release/r220191211.md)  |
| 1.1.5.8 | snapshot | 20200606   | 1. add contentType method<br/>2. contentBody using contentType setting             | [go](release/r20200606.md)  |
| 1.1.5.9 | snapshot | 20200914   | httpReturn always set responseBytes             | [go](release/r20200914.md)  |

