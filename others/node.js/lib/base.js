'use strict';

const parse = require('url').parse;
const querystring = require('querystring');

const form_ct = 'application/x-www-form-urlencoded';
const json_ct = 'application/json';
/**
 * CSB HTTP-SDK Client for node.js
 * 实现基类
 */
class Base {
  constructor() {}

  get(url, opts = {}, responseHeaders) {
    var parsed = parse(url, true);
    var maybeQuery = opts.query || opts.data;
    if (maybeQuery) {
      // append data into querystring
      Object.assign(parsed.query, maybeQuery);
      parsed.path = parsed.pathname + '?' + querystring.stringify(parsed.query);
      opts.data = null;
      opts.query = null;
    }

    return this.request('GET', parsed, opts, null, responseHeaders);
  }

  post(url, opts = {}, responseHeaders) {
    var parsed = parse(url, true);
    var query = opts.query;
    if (query) {
      // append data into querystring
      Object.assign(parsed.query, query);
      parsed.path = parsed.pathname + '?' + querystring.stringify(parsed.query);
      opts.query = null;
    }

    var headers = opts.headers;
    var type = headers['content-type'] || headers['Content-Type'];
    if (!type) {
      headers['content-type'] = form_ct;
      type = headers['content-type'];
    }

    var originData = "" ;
    if (type.startsWith(form_ct)) {
      originData = querystring.stringify(opts.data);
      opts.data = originData;
    } else if (type.startsWith(json_ct)) {
      opts.data = JSON.stringify(opts.data);
    } else if (!Buffer.isBuffer(opts.data) && typeof opts.data !== 'string') {
      // 非buffer和字符串时，以JSON.stringify()序列化 TODO: test byte[]
      opts.data = JSON.stringify(opts.data);
    }

    return this.request('POST', parsed, opts, originData, responseHeaders);
  }
}

module.exports = Base;
