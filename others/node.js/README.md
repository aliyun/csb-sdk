Aliware CSB HTTP-SDK for Node.js
==================================

该工具包提供node.js方式调由云服务总线（CSB）开放出来的RESTful API.

## Status Code

Git Source: [https://github.com/aliyun/csb-sdk/tree/master/others/node.js](https://github.com/aliyun/csb-sdk/tree/master/others/node.js)

## Installation

You can install it as dependency with npm.

```sh
$ # save into package.json dependencies
$ npm install aliware-csb -g
```

## Usage

The SDK contains Simple client, user can invoke it with post or get method.

```js
'use strict';

const co = require('co');

const {
    CONTENT_TYPE_FORM,
    Client
    } = require('aliware-csb');

const client = new Client();

//使用Client类的一个具体的例子,详细功能参见各个字段的备注
co(function* () {
    var url = 'http://your-broker-ip:8086/CSB?name=wiseking';
    var responseHeaders = {}; //初始化kv存储, 用来返回调用后的http response headers

    var result = yield client.post(  //支持client.get() 和 client.post() 两种方式调用
        url,   //参数1: 请求地址 可以携带请求参数, e.g. http://broker-ip:8086/CSB?name=abc
        //参数2: 如下的opts结构:
        {
        api:"PING",        //服务api全名
        version: "vcsb",   //服务api的版本
        headers: {   //请求的http-headers 可选,将由CSB透传到接入端服务
            'content-type': CONTENT_TYPE_FORM  //不同的content-type决定请求的类型: 1. 默认,form参数请求 2. json请求  3. bytes请求
        },
        query: {    //请求参数, 它们将在内部并作为URL参数拼接到请求URL中
            'a-query1': 'query1Value',
            'b-query2': 'query2Value'
        },
        data: {    //body参数
            'a-body1': 'body1Value',
            'b-body2': 'body2Value'
        }
        },
        responseHeaders  //参数3: 用来返回http response headers的信息
    );

    console.log(result);  //调用返回的结果串
    console.log("responeHeaders=" + JSON.stringify(responseHeaders));
});
```

## Question?

Please submit an issue.

## License

The MIT License