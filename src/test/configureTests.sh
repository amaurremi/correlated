#!/bin/bash

# Usage:
# configureTests <JRE rt.jar path>

testroot=scala/ca/uwaterloo/ide
testdirs="$testroot/cp $testroot/taint"

root=`pwd`
if [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    root="c:/Users/mrapopor/Thesis/project/Correlated Calls/src/test"
fi

function createJar() {
    echo $1
    rm $1/*.class
    rm $1/*.jar
    javac $1/*.java
    jar cvf $1.jar $1/*.class
    mv $1.jar $1
    rm $1/*.class
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
       class = \"L$testname/$testname\"\n
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
    for test in `ls -d $testdir/inputPrograms/*/` ; do
        testname=`basename $test`
        echo -n `basename $test`...
        cd "$test/.."
        createJar $testname > /dev/null 2>&1
        cd "$root"
        createConfigFile `basename $testdir` $testname "$jrepath"
        echo "[DONE]"
    done
done


