call mvn clean
mkdir target\classes
call mvn assembly:assembly -Dmaven.test.skip -Dfile.encoding=UTF-8
type target\classes\csb-sdk-version.properties
