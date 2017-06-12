import java.util.HashMap;
import java.util.Map;

import com.alibaba.csb.sdk.HttpCaller;
import com.alibaba.csb.sdk.HttpCallerException;

public class MyRun {

	public static void main(String[] args) {
		String requestURL = "http://10.125.50.237:8086/test?name=a&age=12&title=test";
			String apiName = "httpjson_target2";
			String version = "1.0.0";
			String ak ="ak";
			String sk = "sk";
			
			requestURL = "http://localhost:8086/service";
			apiName =  "getxmltoken_gdic154"; 
			Map<String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put("key","af514f8170be43ea93ea70f9d43ca6f2");
			paramsMap.put("signature","4O4xAxEh8RSCuyq6U3Yb9HwhCIE=");
			paramsMap.put("secret","gdjrb888");
	        ak="2fba3c68960944089d1d61a3929474f1";
	        sk="OeCjAkzI2DVwqKSb2yxpVVrGyKU=";
			try {
				String result = HttpCaller.doPost(requestURL, apiName, version, paramsMap, ak, sk);
				System.out.println(result);
			} catch (HttpCallerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
