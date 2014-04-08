#!/bin/bash

# Usage:
# configureTests <JRE rt.jar path>

testroot=ca/uwaterloo/ide
testdirs="$testroot/cp $testroot/taint"

root=`pwd`
if [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    root="c:/Users/mrapopor/Thesis/project/Correlated Calls/src/test"
fi

function createJar() {
    testpath=$1
    testname=$2
    cd scala
    rm -rf $testpath/*.class
    rm -rf $testpath/*.jar
    javac $testpath/*.java
    jar cvf "$testpath$testname.jar" $testpath/*.class
    rm -rf $testpath/*.class
    cd "$root"
}

function createConfigFile() {
    testdir=$1
    testname=$2
    jrepath=$3
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

for testdir in $testdirs ; do
    echo "Configuring tests for [$testdir]"
    jrepath=$1
    cd scala
    for test in `ls -d $testdir/inputPrograms/*/` ; do
        testname=`basename $test`
        echo -n `basename $test`...
        createJar $test $testname > /dev/null 2>&1
        createConfigFile `basename $testdir` $testname "$jrepath"
        echo "[DONE]"
    done
    cd "$root"
done
