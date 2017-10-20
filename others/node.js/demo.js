/**
 * 使用node.js CSB-HTTP-SDK 的测试用例
 *
 * Created by wiseking on 2017/9/30.
 */
'use strict';

const co = require('co');

const {
    CONTENT_TYPE_FORM,
    Client
    } = require('./');

const client = new Client();

//使用Client类的一个具体的例子,详细功能参见各个字段的备注
co(function* () {
    var url = 'http://localhost:8086/CSB?name=wiseking';
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


co(function* () {
    var url = 'http://localhost:8086/CSB?name=wiseking';
    var responseHeaders = {};
    var result = yield client.get(url, {
            api:"testa",
            version: "1.0.0",
            accessKey:"4f196fb61c1f46ffbf71691ffad35dbb",
            secretKey:"Bzw2YO0HBXFMpcd9CN8tzeNrmf0=",
            headers: {
                'content-type': CONTENT_TYPE_FORM
            },
            query: {
                'a-query1': 'query1Value',
                'b-query2': 'query2Value'
            },
            data: {
                'a-body1': 'body1Value',
                'b-body2': 'body2Value'
            }
        }
        , responseHeaders);

    console.log(result);
    //console.log("responeHeaders="+JSON.stringify(responseHeaders));
});
