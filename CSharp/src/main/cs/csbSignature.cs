using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace csb_signature
{
    class Program
    {
        static void Main(string[] args)
        {
            Dictionary<string, object[]> formParamDict = new Dictionary<string, object[]>();
            formParamDict.Add("name",new string[] { "中文name1" });
            formParamDict.Add("times", new object[] { 123 });
            formParamDict.Add("multiValues", new object[] { "abc","efg" });
            // long timeStamp = DateTimeOffset.Now.ToUnixTimeSeconds();
            long timeStamp = 1592225468715;

            //form表单提交的签名串生成示例
            string signature = sign("http2http11", "1.0.0", timeStamp, "ak值", "sk值", formParamDict, null);
            Console.WriteLine("form signature::::: "+signature);

            //json或xml文本提交的签名串生成示例
            signature = sign("http2http11", "1.0.0", timeStamp, "ak值", "sk值", null, "{\"name\":\"中文name1\", \"times\":\"123\" }");
            Console.WriteLine("json signature::::: " + signature);
        }



        /// <summary>
        /// 本方法生成http请求的csb签名值。
        /// 调用csb服务时，需要在httpheader中增加以下几个头信息：
        /// _api_name: csb服务名
        /// _api_version: csb服务版本号
        /// _api_access_key: csb上的凭证ak
        /// _api_timestamp: 时间戳
        /// _api_signature: 本方法返回的签名串
        /// </summary>
        /// <param name="apiName">csb服务名</param>
        /// <param name="apiVersion">csb服务版本号</param>
        /// <param name="timeStamp">时间戳</param>
        /// <param name="accessKey">csb上的凭证ak</param>
        /// <param name="secretKey">csb上凭证的sk</param>
        /// <param name="formParamDict">form表单提交的参数列表(各参数值是还未urlEncoding编码的原始业务参数值)。如果是form提交，请使用 Content-Type= application/x-www-form-urlencoded </param>
        /// <param name="body">非form表单方式提交的请求内容，目前没有参与签名计算</param>
        /// <returns>签名串。</returns>
        public static string sign(string apiName,string apiVersion, long timeStamp,string accessKey,string secretKey, Dictionary<string,  object[]> formParamDict, object body)
        {
            Dictionary<string, object[]> newDict = new Dictionary<string, object[]>();
            if (formParamDict != null) {
                foreach (KeyValuePair<string, object[]> pair in formParamDict)
                {
                    newDict.Add(pair.Key,pair.Value);
                }
            }

            //设置csb要求的头参数
            newDict.Add("_api_name",new String[] { apiName });
            newDict.Add("_api_version", new String[] { apiVersion });
            newDict.Add("_api_access_key", new String[] { accessKey });
            newDict.Add("_api_timestamp", new object[] { timeStamp });

            //对所有参数进行排序
            var sortedDict = from pair in newDict orderby pair.Key select pair;
            StringBuilder builder = new StringBuilder();
            foreach (KeyValuePair<string, object[]> pair in sortedDict)
            {
                foreach(object obj in pair.Value)
                {
                    builder.Append(string.Format("{0}={1}&", pair.Key, obj));
                }
            }
            string str = builder.ToString();
            if (str.EndsWith("&"))
            {
                str = str.Substring(0, str.Length - 1); //去掉最后一个多余的 & 符号
            }
            System.Security.Cryptography.HMACSHA1 hmacsha = new System.Security.Cryptography.HMACSHA1
            {
                Key = Encoding.UTF8.GetBytes(secretKey)
            };
            byte[] bytes = Encoding.UTF8.GetBytes(str);
            return Convert.ToBase64String(hmacsha.ComputeHash(bytes));
        }
    }
}
