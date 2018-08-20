mvn clean
TARGET_DIR=target/classes
mkdir -p ${TARGET_DIR}
#git log -n 1
mvn assembly:assembly -Dmaven.test.skip
cat ${TARGET_DIR}/csb-sdk-version.properties
