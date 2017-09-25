/**
Copyright 1999-2017 Alibaba Group Holding Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

CSB-HTTP-SDK based on GO language.

 */
package csbhttp

import (
	"fmt"
	//"crypto/tls"
	"time"
	"net/http"
	"net/url"
	"sync"
	"encoding/json"
	"strings"
	"io/ioutil"
	"bytes"
	"strconv"
	"net"
)

var settingMutex sync.Mutex

const (
	CSB_SDK_VERSION = "1.1.0"

	API_NAME_KEY = "_api_name"
	VERSION_KEY = "_api_version"
	ACCESS_KEY = "_api_access_key"
	SECRET_KEY = "_api_secret_key"
	SIGNATURE_KEY = "_api_signature"
	TIMESTAMP_KEY = "_api_timestamp"
	RESTFUL_PATH_SIGNATURE_KEY = "csb_restful_path_signature_key" //TODO: fix the terrible key name!
)

/**
CSBHttp的基本设置结构
 */
type CSBHTTPSettings struct {
	ShowDebug        bool           //  "运行时是否显示调试信息"
	UserAgent        string         //  "调用CSB服务的客户端代理, 默认为 csbBroker"
	ConnectTimeout   time.Duration  //  "连接超时时间"
	ReadWriteTimeout time.Duration  //  "读写超时时间"

	Retries          					int  // if set to -1 means will retry forever
	CareResponseHttpHeader    bool // if return the response http headers
	SignPath				 					bool


																	/* TODO:support the following fields
	TLSClientConfig  *tls.Config
	Proxy            func(*http.Request) (*url.URL, error)
	Transport        http.RoundTripper
	CheckRedirect    func(req *http.Request, via []*http.Request) error
	EnableCookie     bool
	*/
}

var defaultSetting = CSBHTTPSettings{
	ShowDebug:				true,
	UserAgent:        "csbBroker",
	ConnectTimeout:   60 * time.Second,
	ReadWriteTimeout: 60 * time.Second,
	CareResponseHttpHeader:    true,
}

/**
  定义自己的http属性的结构来覆盖默认的设置
 */
func SetDefaultSetting(setting CSBHTTPSettings) {
	settingMutex.Lock()
	defer settingMutex.Unlock()
	defaultSetting = setting
}

/**
内部方法: 拼接请求参数
 */
func appendParams(reqUrl string, params string) string {
	if (strings.Contains(reqUrl, "?")) {
		return reqUrl + "&" + params;
	}else {
		return reqUrl + "?" + params;
	}
}

/**
内部方法: 将请求串中的请求参数装换为map
 */
func parseUrlParamsMap(reqUrl string) (params map[string]string, err *HttpCallerException) {
	params = make(map[string]string) //must init the map
	if strings.Contains(reqUrl, "?") {
		i := strings.Index(reqUrl, "?")
		reqUrl = string(reqUrl[i+1:]);
		fmt.Println(reqUrl)
		kvs := strings.Split(reqUrl, "&")

		if len(kvs) > 0 {
			for _, kv := range kvs {
				i = strings.Index(kv, "=")
				if (i >= 0) {
					params[string(kv[0:i])] = string(kv[i+1:])
				}else {
					//TODO: write or throw exception
					fmt.Errorf("bad kv pair:", kv)
				}
			}
		}
	}
	return params, nil
}

/**
内部方法: 进行参数的签名处理
 */
func signParams(params map[string]string, api string, version string, ak string, sk string) (headMaps map[string]string) {
	headMaps = make(map[string]string)

	params[API_NAME_KEY] = api;
	headMaps[API_NAME_KEY] = api;

	params[VERSION_KEY] = version;
	headMaps[VERSION_KEY] = version;

	//https://currentmillis.com/  calc current time with varies languages
	v := time.Now().UnixNano() / 1000000
	params[TIMESTAMP_KEY] = strconv.FormatInt(v,10)
	headMaps[TIMESTAMP_KEY] = strconv.FormatInt(v,10)

	if ak != "" {
		params[ACCESS_KEY] = ak
		headMaps[ACCESS_KEY] = ak

		delete(params, SECRET_KEY)
		delete(params, SIGNATURE_KEY)

		signValue := doSign(params, sk)

		headMaps[SIGNATURE_KEY] = signValue
	}

	return headMaps
}

/**
 调用CSB开放出来的服务(后者CSB控制台的Open API),并放回结果

 请求参数的内容根据 HttpParams 的定义进行设置

 返回的结果包含: 调用fa返回结果串, 返回的httpheaders 和 异常
 当处理正常时,异常为nil

 */
func Invoke(params HttpParams) (str string, rtnHeaders map[string][]string, hcError *HttpCallerException) {
	//init rtnHeaders
	rtnHeaders = make(map[string][]string)

	hcError = params.Validate();
	if hcError != nil {
		return str, rtnHeaders, hcError;
	}

	_, err := url.Parse(params.requestUrl)
	if err != nil {
		return str, rtnHeaders, &HttpCallerException{CauseErr : err}
	}

	client := &http.Client{
		Transport: &http.Transport{
			Dial: func(netw, addr string) (net.Conn, error) {
				conn, err := net.DialTimeout(netw, addr, defaultSetting.ConnectTimeout)    //设置建立连接超时
				if err != nil {
					return nil, err
				}
				conn.SetDeadline(time.Now().Add(defaultSetting.ReadWriteTimeout))    //设置发送接受数据超时
				return conn, nil
			},
			ResponseHeaderTimeout: time.Second * 2,
		},
	}

	//resp := &http.Response{}
	//for request parmas
	data := url.Values{}
	if params.params != nil {
		for k, v := range params.params {
			//strParams += fmt.Sprintf("%s=%s", k, url.QueryEscape())
			data.Set(k, v);
		}
	}
	var reqStr string;
	var method string;
	//method := "GET"
	if params.method == "post" {
		//genSignHeaders
		method = "POST"
	}else {
		method = "GET"
	}

	reqUrl := params.requestUrl

	defaultContentType := "application/x-www-form-urlencoded"
	if params.ct.jsonBody != "" {
		_, err = json.Marshal(params.ct.jsonBody)
		if err == nil {
			return str, rtnHeaders, &HttpCallerException{Message : "failed to bad content type json string", CauseErr : err}
		}
		reqStr = params.ct.jsonBody
		defaultContentType = params.ct.contentType;
		reqUrl = appendParams(reqUrl, data.Encode())
	}else if params.ct.bytesBody != nil {
		reqStr = string(params.ct.bytesBody)
		defaultContentType = params.ct.contentType;
		reqUrl = appendParams(reqUrl, data.Encode())
	}else {
		reqStr = data.Encode();
	}

	req, err := http.NewRequest(method, reqUrl, bytes.NewBufferString(reqStr))

	if err != nil {
		return str, rtnHeaders, &HttpCallerException{Message : "failed to construct http post request", CauseErr : err}
	}

	//set signature related headers
	urlParams, hcError := parseUrlParamsMap(params.requestUrl)
	if err != nil {
		return str, rtnHeaders, hcError
	}
	printDebug("urlparams", urlParams)
	mergeTwoMaps(urlParams, params.params)
	printDebug("merged urlparams", urlParams)
	signHeaders := signParams(urlParams, params.api, params.version, params.ak, params.sk);
	printDebug("signHeaders", signHeaders)


	req.Header.Add("Content-Type", defaultContentType)
	if params.headers != nil {
		for k, v := range params.headers {
			req.Header.Add(k, v);
		}
	}
	//add sign related headers
	if signHeaders != nil {
		for k, v := range signHeaders {
			req.Header.Add(k, v);
		}
	}

//client.Timeout
	resp, err := client.Do(req)

	if err != nil {
		return str, rtnHeaders, &HttpCallerException{Message : "failed to invoke http post", CauseErr : err}
	}

	if err != nil {
		fmt.Println(err)
	}

	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Println(err)
	}
	str = string(body)
	if defaultSetting.CareResponseHttpHeader {
		rtnHeaders = resp.Header
	}
	/*
		fmt.Println("jsonStr", jsonStr)
		var dat map[string]string
		if err := json.Unmarshal([]byte(jsonStr), &dat); err == nil {
			fmt.Println("token", dat["token"])
		} else {
			fmt.Println("json str to struct error")
		}
		*/
	return str, rtnHeaders, nil;
}
