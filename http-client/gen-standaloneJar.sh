mvn clean
TARGET_DIR=target/classes
mkdir -p ${TARGET_DIR}
echo "commit=`git rev-parse --verify HEAD`" > ${TARGET_DIR}/csb-sdk-version.properties
echo "build_time=`date`" >> ${TARGET_DIR}/csb-sdk-version.properties
cat ${TARGET_DIR}/csb-sdk-version.properties
#git log -n 1
# -P jdk1.6
mvn assembly:assembly -P jdk1.5 -Dmaven.test.skip -s ../settings.xml.csb-sdk 
