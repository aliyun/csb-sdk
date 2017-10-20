'use strict';

function supportAsyncFunctions() {
  try {
    new Function('(async function () {})()');
    //console.log("async okay")
    return true;
  } catch (ex) {
   // console.log("async not okay")
    return false;
  }
}

const asyncSupported = supportAsyncFunctions();

//always use not async implement by now
exports.Client = asyncSupported ? require('./lib/client') : require('./lib/client');

//表单类型Content-Type
exports.CONTENT_TYPE_FORM = 'application/x-www-form-urlencoded; charset=UTF-8';
// 流类型Content-Type
exports.CONTENT_TYPE_STREAM = 'application/octet-stream; charset=UTF-8';
//JSON类型Content-Type
exports.CONTENT_TYPE_JSON = 'application/json; charset=UTF-8';
//XML类型Content-Type
exports.CONTENT_TYPE_XML = 'application/xml; charset=UTF-8';
//文本类型Content-Type
exports.CONTENT_TYPE_TEXT = 'application/text; charset=UTF-8';
