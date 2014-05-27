#!/bin/bash

# Usage:
# configureTests <JRE rt.jar path>

### Replace the `jrepath' value with your path to the rt.jar file
jrepath="C:/Program Files (x86)/Java/jdk1.6.0_45/jre/lib/rt.jar"

testroot=ca/uwaterloo/dataflow/ide/taint

root=`pwd`
if [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    root="c:/Users/mrapopor/Thesis/project/Correlated Calls/src/test"
fi

function createJar() {
    testname=$2
    testpath=$1/$testname
    testparent=$1
    rm -rf $testpath/*.class
    rm -rf $testpath/*.jar
    javac -g $testpath/*.java
    jar cvf "$testpath/$testname.jar" $testpath/*.class $testparent/*.class
    cd "$root"
}

function createConfigFile() {
    testname=$1
    dir="resources/ca/uwaterloo/dataflow/ide/taint"
    mkdir -p $dir
    cd $dir
    testpath="$root/scala/ca/uwaterloo/dataflow/ide/taint/inputPrograms/$testname/$testname.jar"
    contents="
    wala {\n
      jre-lib-path = \"$jrepath\"\n
      dependencies.jar += \"$testpath\"\n
      entry {\n
       class = \"Lca/uwaterloo/dataflow/ide/taint/inputPrograms/$testname/$testname\"\n
       method = \"main([Ljava/lang/String;)V\"\n
      }\n
    }\n
    "
    echo -e $contents > $testname.conf
    cd "$root"
}

cd scala
for test in `ls -d $testroot/inputPrograms/*/` ; do
    testname=`basename $test`
    echo -n `basename $test`...
    testParent=`dirname $test`
    cd scala
    createJar $testParent $testname > /dev/null 2>&1
    createConfigFile $testname
    echo "[DONE]"
done
cd "$root"