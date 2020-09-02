#!/usr/bin/env python
# -*- coding:utf-8 -*-
# Author:   huYong
# Mail:     18166035355@163.com
# Date:     2020/6/6 1:40


import hashlib
import hmac
from aliyunsdkcore.compat import b64_encode_bytes


def ensure_bytes(s, encoding='utf-8', errors='strict'):
    if isinstance(s, str):
        return bytes(s, encoding=encoding)
    if isinstance(s, bytes):
        return s
    if isinstance(s, bytearray):
        return bytes(s)
    raise ValueError(
        "Expected str or bytes or bytearray, received %s." %
        type(s))


def ensure_string(s, encoding='utf-8', errors='strict'):
    if isinstance(s, str):
        return s
    if isinstance(s, (bytes, bytearray)):
        return str(s, encoding='utf-8')
    raise ValueError(
        "Expected str or bytes or bytearray, received %s." %
        type(s))


def get_sign_string(source, secret):
    source = ensure_bytes(source)
    secret = ensure_bytes(secret)
    h = hmac.new(secret, source, hashlib.sha1)
    signature = ensure_string(b64_encode_bytes(h.digest()).strip())
    return signature


def get_signature(info_dict: dict, sk):
    """
    :param info_dict:   eg:
    {
        '_api_access_key': 'xxx',
        '_api_name': 'xxx',
        '_api_timestamp': 'xxx',
        '_api_version': '1.0.0',
        'query_param': 'x',  # 可选
    }

    :return: signature
    """

    info_key_list = sorted([str(x) + '=' + (str(y) if y else '""') for x, y in info_dict.items()])
    source_str = '&'.join(info_key_list)
    print(source_str)

    signature = get_sign_string(source_str, sk)
    return signature


source = {
    '_api_access_key': 'ak...',
    '_api_name': 'GetSYDW',
    '_api_timestamp': 1591343620544,
    '_api_version': 12.18,
    # 'args': ''
    # 'a': '1',  # 可选
}

_api_signature = get_signature(source, sk='sk...')
print(_api_signature)
