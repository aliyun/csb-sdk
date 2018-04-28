'use strict';

const {parse, format} = require('url');
const crypto = require('crypto');

//const uuid = require('uuid');
const httpx = require('httpx');
const debug = require('debug')('csb');

const ua = require('./ua');
const Base = require('./base');

//const form = 'application/x-www-form-urlencoded';

const header_api = '_api_name';
const header_version = '_api_version';
const header_ak = '_api_access_key';
const header_timestamp = '_api_timestamp';
const header_sig = '_api_signature';

const hasOwnProperty = function (obj, key) {
    return Object.prototype.hasOwnProperty.call(obj, key);
};

/**
 * CSB HTTP-SDK node.js Client
 */
class Client extends Base {
    constructor(stage = 'RELEASE') {
        super();

        this.stage = stage;
    }

    sign(stringToSign, secretKey) {
        var appSecret = Buffer.from(secretKey, 'utf8');
        return crypto.createHmac('sha1', appSecret)
            .update(stringToSign, 'utf8').digest('base64');
    }

    md5(content) {
        return crypto.createHash('md5')
            .update(content, 'utf8')
            .digest('base64');
    }

    splitQuery(str) {
        //console.log("aaaa" + JSON.stringify(str))
        var rtn = {};
        if(str) {
           var kvs = str.toString().split("&");
            for (var i = 0; i < kvs.length; i++) {
                var iPos = kvs[i].indexOf("=");
                if(iPos>=0) {
                    rtn[kvs[i].substr(0, iPos)] = kvs[i].substr(iPos+1);
                }
            }
        }
        return rtn;
    }

    buildStringToSign(query, data, signHeaders) {
        var arr = {};
        var params = this.splitQuery(data);
        //console.log("params=", params);
        var toStringify = Object.assign(arr, query, params, signHeaders);
        //console.log(toStringify);

        var result = ''; //parsedUrl.pathname;
        if (Object.keys(toStringify).length) {
            var keys = Object.keys(toStringify).sort();
            var list = new Array(keys.length);
            for (var i = 0; i < keys.length; i++) {
                var key = keys[i];
                if (toStringify[key] && ('' + toStringify[key])) {
                    list[i] = `${key}=${toStringify[key]}`;
                } else {
                    list[i] = `${key}`;  //TODO: need this ??
                }
            }
            result += list.join('&');
        }
        return result;
    }

    buildSignHeaders(api, version, ak) {
        var headers = new Array();
        headers[header_timestamp] = new Date().getTime();
        headers[header_api] = api;
        headers[header_version] = version;
        if (ak != null) { //is null or undefined
            headers[header_ak] = ak;
        }

        return headers;
    }

    getSignedHeadersString(api, version, ak, sk, query, originData) {
        var signHeaders = this.buildSignHeaders(api, version, ak)
        if (ak != null) {
            //do sign process
            var stringToSign = this.buildStringToSign(query, originData, signHeaders);
            var signValue = this.sign(stringToSign, sk);
            signHeaders[header_sig] = signValue;
        }

        return signHeaders;
    }

    validateOpts(opts) {
        var msg = '';
        if (opts.api == null) {
            msg += "bad params, must set opts.api! ";
        }
        if (opts.version == null) {
            msg += "bad params, must set opts.version! ";
        }
        if (opts.accessKey != null && opts.secretKey == null) {
            msg += "bad params, must set opts.secretKey if opts.accessKey has been set! ";
        }


        if(msg != '') {
            throw new Error(msg);
        }
    }

    //async
    request(method, parsedUrl, opts, originData, responseHttpHeaders) {
        this.validateOpts(opts);
        var signHeaders = this.getSignedHeadersString(opts.api, opts.version, opts.accessKey, opts.secretKey, parsedUrl.query, originData);
        var headers = opts.headers || {};
        headers['user-agent'] = ua;
        Object.assign(headers, signHeaders);
        //console.log(headers);

        if (debug.enabled) {
            debug('post body:');
            debug('%s', opts.data);
        }

        var entry = {
           // url: parsedUrl.url ,
            request: null,
            response: null
        };
        var scode = 100;

        //await
        var res = httpx.request(parsedUrl, {
            method: method,
            headers: headers,
            data: opts.data,
            timeout: opts.timeout
        }).then((response) => {
            entry.request = {
            //commet out this line for runtime error in react
            //headers: response.req._headers
            };
            entry.response = {
            statusCode: response.statusCode,
            headers: response.headers
            };

        return httpx.read(response);
    }).then((buffer) => {
    //console.log("buffer="+buffer);
    var code = entry.response.statusCode;
    //console.log("code="+code);
    if (code !== 200) {
    var err = new Error(buffer);
    err.entry = entry;
      return Promise.reject(err);
    };

    Object.assign(responseHttpHeaders, entry.response.headers);

    if (this.verbose) {
      return [buffer+"", entry];
    }

    return buffer+"";
});


        //await
        //var result = httpx.read(response, 'utf8');
        /*
        var contentType = response.headers['content-type'] || '';
        if (contentType.startsWith('application/json')) {
            result = JSON.parse(result);
        }
        */

        return res;
    }
}

module.exports = Client;
