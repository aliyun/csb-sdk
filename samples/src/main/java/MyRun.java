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
			paramsMap.put("key","replace_on_test");
			paramsMap.put("signature","replace_on_test";
			paramsMap.put("secret","gdjrb888");
	        ak="replace_on_test";
	        sk="replace_on_testg";
			try {
				String result = HttpCaller.doPost(requestURL, apiName, version, paramsMap, ak, sk);
				System.out.println(result);
			} catch (HttpCallerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
