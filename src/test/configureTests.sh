#!/bin/bash

# Usage:
# configureTests <JRE rt.jar path>

### Replace the `jrepath' value with your path to the rt.jar file
jrepath="C:/Program Files (x86)/Java/jdk1.6.0_45/jre/lib/rt.jar"

testroot=ca/uwaterloo/dataflow/ide
testdirs="$testroot/cp $testroot/taint"

root=`pwd`
if [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    root="c:/Users/mrapopor/Thesis/project/Correlated Calls/src/test"
fi

function createJar() {
    testpath=$1
    testname=$2
    testParent=$1/$testname
    cd scala
    rm -rf $testpath/*.class
    rm -rf $testpath/*.jar
    javac -g $testpath/*.java
    jar cvf "$testpath$testname.jar" $testpath/*.class $testParent/*.class
    cd "$root"
}

function createConfigFile() {
    testdir=$1
    testname=$2
    dir="resources/ca/uwaterloo/dataflow/ide/$testdir"
    mkdir -p $dir
    cd $dir
    testpath="$root/scala/ca/uwaterloo/dataflow/ide/$testdir/inputPrograms/$testname/$testname.jar"
    contents="
    wala {\n
      jre-lib-path = \"$jrepath\"\n
      dependencies.jar += \"$testpath\"\n
      entry {\n
       class = \"Lca/uwaterloo/dataflow/ide/$testdir/inputPrograms/$testname/$testname\"\n
       method = \"main([Ljava/lang/String;)V\"\n
      }\n
    }\n
    "
    echo -e $contents > $testname.conf
    cd "$root"
}

for testdir in $testdirs ; do
    echo "Configuring tests for [$testdir]"
    cd scala
    for test in `ls -d $testdir/inputPrograms/*/` ; do
        testname=`basename $test`
        echo -n `basename $test`...
        createJar $test $testname > /dev/null 2>&1
        createConfigFile `basename $testdir` $testname
        echo "[DONE]"
    done
    cd "$root"
done
