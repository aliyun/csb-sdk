TARGET_DIR=target/classes
mkdir -p ${TARGET_DIR}
echo "commit=`git rev-parse --verify HEAD`" > ${TARGET_DIR}/version.properties
echo "build_time=`date`" >> ${TARGET_DIR}/version.properties
cat ${TARGET_DIR}/version.properties
#git log -n 1
