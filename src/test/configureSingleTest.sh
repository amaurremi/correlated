#!/bin/bash

# Usage:
# configureTest <JRE rt.jar path> <test name>

### Replace the `jrepath' value with your path to the rt.jar file
jrepath="/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar"

testroot=ca/uwaterloo/dataflow/ide/taint
testParent=$testroot/inputPrograms

root=`pwd`
if [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    root="c:/Users/mrapopor/Thesis/project/Correlated Calls/src/test"
fi

function createJar() {
    testname=$1
    testpath=$testParent/$testname
    rm -rf $testpath/*.class
    rm -rf $testpath/*.jar
    javac -g $testpath/*.java
    jar cvf "$testpath/$testname.jar" $testpath/*.class $testParent/*.class
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

testname=$1
test=$testParent/$testname
cd scala
echo -n $testname...
createJar $testname > /dev/null 2>&1
createConfigFile $testname
echo "[DONE]"