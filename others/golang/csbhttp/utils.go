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
  工具相关的方法定义在这个文件中
 */
// print debug info
func printDebug(prompt string, a ...interface{}) {
	if defaultSetting.ShowDebug {
		fmt.Println(prompt, a);
	}
}

// merge two maps
func mergeTwoMaps(toMap map[string]string, fromMap map[string]string) {
	for k, v := range fromMap {
		toMap[k] = v;
	}
}
