<!DOCTYPE html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<html>
    <body>        
       <?php  require 'http-caller.php';
       
        // 使用phpSDK  HttpCalle的测试参考代码  
        echo "<br>PHP SDK Usage to invoke http-API provided by CSB:";
           
        echo "<hr>";
        $url =  "http://11.239.187.178:8086/httpjson";
        $data = array('name'=>'wiseking', 'age'=>'100','title'=>'god');
       
        $api =  "httpjson"; 
        $version = '1.0.0';
        
        $ak = 'ak'; 
        $sk = 'sk';
        
       echo '<br>';
       
       $phpCaller = new HttpCaller();
       
       //打印debug信息
       $phpCaller->PRINT_DEBUG = false;
       
       try{
         //测试GET调用
         echo "<br>Test Get....<br>";
         $result = $phpCaller->doGet($url, $data, $api, $version, $ak, $sk);
         echo "<br>result=".$result."<br>";
         echo "<hr>";
         
         //测试POST调用
         echo "<br>Test Post kvpairs....<br>";
         $result = $phpCaller->doPost($url, $data, $api, $version, $ak, $sk);
         echo "<br>result=".$result."<br>";
         echo "<hr>";
         
          //测试发送json串调用
         echo "<br>Test Post httpjsonbody....<br>";
         $url="http://11.239.187.178:8086/test?name=a&age=12&title=test";
         $jsonStr = "{\"a\":\"csb云服务总线\"}";
         $api = "httpjsonbody";
         $result = $phpCaller->doPostJsonString($url, $jsonStr, $api, $version, $ak, $sk);
         echo "<br>result=".$result."<br>";
         echo "<hr>";
         
         //测试发送二进制数组
         echo "<br>Test Post byte array....<br>";
         $url="http://11.239.187.178:8086/test?fileName=test.pdf&filePath=/home/admin/";
         $byteArray =  $phpCaller->readFileAsByteArray("/ltwork/csb-install/r.sh");
         $api = "httpfile";
         $result = $phpCaller->doPostByteArray($url, $byteArray, $api, $version, $ak, $sk);
         echo "<br>result=".$result."<br>";
         echo "<hr>";
         
         // 进行后续的结果处理
         // ...
      } catch (customException $e) 
      { 
         echo $e->errorMessage(); 
      } catch(Exception $e) 
      { 
         echo $e->getMessage(); 
      } 
    
    ?>
    </body>
</html>
