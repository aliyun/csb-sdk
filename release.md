## Latest version/最新的版本
* RELEASE
 1.  ${http.sdk.version} = 1.1.4.1
 2.  ${ws.sdk.version} = 1.1.4.1
* SNAPSHOT
 1.  ${http.sdk.snapshot.version} = 1.1.4-SNAPSHOT
 2.  ${ws.sdk.snapshot.version} = 1.1.4-SNAPSHOT

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
|         |        |                |                                      |         |

