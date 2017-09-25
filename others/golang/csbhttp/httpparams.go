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

import ()
import (
	"strings"
	"encoding/json"
	"fmt"
)

/**
构造Http请求参数的struct
 */
const (
	JSON = "application/json"
	BINARY = "application/octet-stream"
)

type contentBody struct {
	jsonBody    string
	bytesBody   []byte
	contentType string
}

type (
	HttpParams struct {
		method     string   // "设置方法: get, post"
		api        string
		version    string
		ak         string
		sk         string
		requestUrl string
		params     map[string]string  // "form 参数对"
		headers    map[string]string  // "http headers"
		ct         contentBody       //  "设置传输的jsonBody 或者 byte[]"
	}
	Builder func(map[string]HttpParams) string
)

func NewHttpParams(reqUrl string) *HttpParams {
	hp := HttpParams{
		requestUrl:reqUrl,
		method:"get",
		params:make(map[string]string),
		headers:make(map[string]string),
	}

	hp.ct.contentType = "unknown"

	return &hp
}

func (hp *HttpParams) SetApi(api string) (*HttpParams) {
	hp.api = api;
	return hp
}

func (hp *HttpParams) SetVersion(version string) (*HttpParams) {
	hp.version = version;
	return hp
}

func (hp *HttpParams) SetAK(ak string) (*HttpParams) {
	hp.ak = ak;
	return hp
}

func (hp *HttpParams) SetSK(sk string) (*HttpParams) {
	hp.sk = sk;
	return hp
}

func (hp *HttpParams) SetRequest(reqUrl string) (*HttpParams) {
	hp.requestUrl = reqUrl;
	return hp
}

func (hp *HttpParams) SetMethod(method string) (*HttpParams) {
	hp.method = method;
	return hp
}

func (hp *HttpParams) AddParam(key string, value string) (*HttpParams) {
	hp.params[key] = value;
	return hp
}

func (hp *HttpParams) AddHeader(key string, value string) (*HttpParams) {
	hp.headers[key] = value;
	return hp
}

func (hp *HttpParams) SetContentBody(jsonStr string, byteArr []byte) error {
	if jsonStr != "" && byteArr != nil {
		errRtn := HttpCallerException{Message:"can not set jsonStr and byteArr parameters together!"}
		return errRtn
	}

	if jsonStr != "" {
		hp.ct.jsonBody = jsonStr;
		hp.ct.contentType = JSON;
	}

	if byteArr != nil {
		hp.ct.bytesBody = byteArr;  //TODO: copy or pointer ?
		hp.ct.contentType = BINARY;
	}

	return nil
}

/**
 校验设置的请求参数项是否有效
 */
func (hp *HttpParams) Validate() *HttpCallerException {
	hp.method = strings.ToLower(hp.method);
	if hp.method != "get" && hp.method != "post" {
		return &HttpCallerException{Message:"bad method, only support 'get' or 'post'"}
	}

	if hp.ak != "" && hp.sk == "" {
		return &HttpCallerException{Message:"bad request params, ak and sk must be defined together"}
	}

	if hp.api == "" || hp.version == "" {
		return &HttpCallerException{Message:"bad request params, api or version is not defined"}
	}

	return nil
}

/**
 打印设置的参数值
 */
func (hp *HttpParams) Print() {
	j, _ := json.Marshal(*hp)
	fmt.Println(string(j))
}