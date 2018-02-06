java -jar target/ws-client-1.1.4-SNAPSHOT.jar -skipTimestamp -fingerStr abc \
  -ak 8dc8a70ed9b04decbc1d0e679c855e19 -sk 1cEQgkfMIbxx8M+mfaJJLvoSb0w= \
  -d -wa  http://11.163.137.89:9081/test-ws2ws/1.0.0/ws2ws?wsdl \
  -ea  http://11.163.137.89:9081/test-ws2ws/1.0.0/ws2ws \
  -ns http://ws.hello/ -sname WSTestServiceService -pname WSTestServicePort \
  -rd  '
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.hello/">
   <soapenv:Header/>
   <soapenv:Body>
      <ws:sayHi>
         <!--Optional:-->
         <arg0>wiseking</arg0>
         <arg1>1</arg1>
      </ws:sayHi>
   </soapenv:Body>
</soapenv:Envelope>
'

