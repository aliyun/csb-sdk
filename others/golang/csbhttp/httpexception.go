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


import "fmt"

/**
  CSB调用异常结构
 */
type HttpCallerException struct {
	Message    string   `xml:"Message"`   // CSB给出的详细错误信息
	RequestID  string   `xml:"RequestId"` // 用于唯一标识该次请求的UUID
	CauseErr   error //具体的error
}

// Implement interface error
func (e HttpCallerException) Error() string {
	return fmt.Sprintf("csb: service returned error: ErrorMessage=%s, RequestId=%s, CauseErr=%v", e.Message, e.RequestID, e.CauseErr)
}
