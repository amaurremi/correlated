#!/bin/bash

# Usage:
# configureTest <JRE rt.jar path> <analysis> <test name>

### Replace the `jrepath' value with your path to the rt.jar file
jrepath="C:/Program Files (x86)/Java/jdk1.6.0_45/jre/lib/rt.jar"

testroot=ca/uwaterloo/ide
testdirs="$testroot/cp $testroot/taint"

root=`pwd`
if [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    root="c:/Users/mrapopor/Thesis/project/Correlated Calls/src/test"
fi

function createJar() {
    testpath=$1
    testname=$2
    rm -rf $testpath/*.class
    rm -rf $testpath/*.jar
    javac -g $testpath/*.java
    jar cvf "$testpath/$testname.jar" $testpath/*.class
    cd "$root"
}

function createConfigFile() {
    testdir=$1
    testname=$2
    mkdir -p "resources/ide/analysis/$testdir"
    cd "resources/ide/analysis/$testdir"
    testpath="$root/scala/ca/uwaterloo/ide/$testdir/inputPrograms/$testname/$testname.jar"
    contents="
    wala {\n
      jre-lib-path = \"$jrepath\"\n
      dependencies.jar += \"$testpath\"\n
      entry {\n
       class = \"Lca/uwaterloo/ide/$testdir/inputPrograms/$testname/$testname\"\n
       method = \"main([Ljava/lang/String;)V\"\n
      }\n
    }\n
    "
    echo -e $contents > $testname.conf
    cd "$root"
}

analysis=$1
testname=$2
test=$testroot/$analysis/inputPrograms/$testname
cd scala
testname=`basename $test`
echo -n `basename $test`...
createJar $test $testname #> /dev/null 2>&1
createConfigFile $analysis $testname
echo "[DONE]"
cd "$root"
