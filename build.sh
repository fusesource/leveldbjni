#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd $SCRIPT_DIR

unset CXXFLAGS
unset CFLAGS
unset CXX
export LEVELDB_HOME=`cd deps/leveldb;pwd`
export SNAPPY_HOME=`cd deps/snappy;pwd`
export LEVELDBJNI_HOME="${SCRIPT_DIR}"

echo
echo
echo "**************************"
echo "***  Building LevelDB  ***"
echo "**************************"
cd "${LEVELDB_HOME}"
mkdir -p build && cd build
cmake -DCMAKE_BUILD_TYPE=Release .. && cmake --build .
cp ${LEVELDB_HOME}/build/libleveldb.a ${LEVELDB_HOME}
#TODO Fail if libleveldb.a is not found

echo
echo
echo "*************************"
echo "***  Building Snappy  ***"
echo "******I******************"
cd "${SNAPPY_HOME}"
mkdir -p build
cd build && cmake ../ && make
cp ${SNAPPY_HOME}/build/libsnappy.a ${SNAPPY_HOME}
#TODO Fail if libsnappy.a is not found

echo
echo
echo "***************************"
echo "*** Building LevelDBJNI ***"
echo "***************************"
cd "${LEVELDBJNI_HOME}"
rm -rf leveldbjni-osx/target
export CXXFLAGS="-std=c++11"
export OSX_VERSION="11.3"
export OSX_SDKS_DIR="/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs"
mvn clean install -DskipTests -P download -P osx
