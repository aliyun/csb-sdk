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
	"sort"
	"crypto/sha1"
	"hash"
	"io"
	"crypto/hmac"
	"encoding/base64"
	"bytes"
)

/**
  签名处理逻辑
  将请求map中的key按字典顺序排序,然后使用secretKey进行签名处理
 */


// 用于signParms的字典排序存放容器。
type paramsSorter struct {
	Keys []string
	Vals []string
}

// params map 转换为paramsSorter格式
func newParamsSorter(m map[string]string) *paramsSorter {
	hs := &paramsSorter{
		Keys: make([]string, 0, len(m)),
		Vals: make([]string, 0, len(m)),
	}

	for k, v := range m {
		hs.Keys = append(hs.Keys, k)
		hs.Vals = append(hs.Vals, v)
	}
	return hs
}

// 进行字典顺序排序 sort required method
func (hs *paramsSorter) Sort() {
	sort.Sort(hs)
}

// Additional function for function  sort required method
func (hs *paramsSorter) Len() int {
	return len(hs.Vals)
}

// Additional function for function  sort required method
func (hs *paramsSorter) Less(i,j int) bool {
	return bytes.Compare([]byte(hs.Keys[i]), []byte(hs.Keys[j])) < 0
}

// Additional function for function paramsSorter.
func (hs *paramsSorter) Swap(i, j int) {
	hs.Vals[i], hs.Vals[j] = hs.Vals[j], hs.Vals[i]
	hs.Keys[i], hs.Keys[j] = hs.Keys[j], hs.Keys[i]
}

// 做签名处理
func doSign(params map[string]string, secretKey string) string {
	hs := newParamsSorter(params)

	// Sort the temp by the Ascending Order
	hs.Sort()

	// Get the CanonicalizedOSSHeaders
	canonicalizedParams := ""
	for i := range hs.Keys {
		if i > 0 {
			canonicalizedParams += "&"
		}
		canonicalizedParams += hs.Keys[i] + "=" + hs.Vals[i]
	}

	printDebug("canonicalizedParams", canonicalizedParams)

	signStr := canonicalizedParams;

	h := hmac.New(func() hash.Hash { return sha1.New() }, []byte(secretKey))
	io.WriteString(h, signStr)
	signedStr := base64.StdEncoding.EncodeToString(h.Sum(nil))

	return signedStr
}
