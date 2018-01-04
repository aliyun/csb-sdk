package com.alibaba.csb.sdk;

public class CsbSDKConstants {

	public static final String API_NAME_KEY = "_api_name";
	public static final String VERSION_KEY = "_api_version";
	public static final String ACCESS_KEY = "_api_access_key";
	public static final String SECRET_KEY = "_api_secret_key";
	public static final String SIGNATURE_KEY = "_api_signature";
	public static final String TIMESTAMP_KEY = "_api_timestamp";

	public static final String NONCE_KEY = "_api_nonce";

	public static final String HEADER_NS = "uri:csb.ws";
	public static final String HEADER_FINGERPRINT = "_api_fingerprint";
	public static final String HEADER_MOCK = "mock_response";

	public static final boolean isNonceEnabled = Boolean.parseBoolean(System.getProperty("csb.sdk.nonceEnable", "true"));
}
