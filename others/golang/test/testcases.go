/**
使用httpcaller的例子
 */
package main

import (
	"fmt"
	"../csbhttp"
)

/**
Case 1: 测试调用PING服务
 */
func testPing() {
	//step 1. 设置请求参数
	csbHP := csbhttp.NewHttpParams("http://localhost:8086/CSB?name=wiseking"); //设置请求地址及url参数

	//设置调用方式 get 或者 post
	csbHP.SetMethod("get")

	//设置 调用的服务名和版本
	csbHP.SetApi("PING")
	csbHP.SetVersion("vcsb")
	//你也可以在一行中设置多个属性值,如: csbHP.SetApi("PING").SetVersion("vcsb")

	//添加附加的body参数 (可以定义多条)
	csbHP.AddParam("p1", "dog")
	csbHP.AddParam("p2", "cat")

	//添加附加的http请求头 (可以定义多条)
	csbHP.AddHeader("h1", "wiseking")

	//还可以,设置附加的byte[]或者json请求体
	//csbHP.SetContentBody("{name:wiseking}",bytes)


	//设置请求的ak和sk
	csbHP.SetAK("ak")
	csbHP.SetSK("sk")


	//打印请求参数信息
	csbHP.Print()

	//进行调用, 返回的第二项为resonse http headers (map[string][]string类型)
	res, _, err := csbhttp.Invoke(*csbHP)

	//是否发成错误
	if(err != nil) {
		fmt.Println(err)
		return
	}

	//打印返回结果
	fmt.Println("res=", res)
}

func main() {

	testPing()

}
