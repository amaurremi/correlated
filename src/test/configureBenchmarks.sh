#!/bin/bash

# Usage:
# ./configureBenchmarks

### Replace the `jrepath' value with your path to the rt.jar file
jrepath="/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar"

testroot=scala/ca/uwaterloo/dataflow/benchmarks/dacapo

root=`pwd`
if [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    root="c:/Users/mrapopor/Thesis/project/Correlated Calls/src/test"
fi

function createConfigFile() {
    testname=$1
    dir="resources/ca/uwaterloo/dataflow/benchmarks/dacapo"
    mkdir -p $dir
    cd $dir
    testdir=$root/$testroot
    testpathSrc=$testdir/sources/$testname
    testpathDep=$testdir/deps/$testname
    contents="
    wala {\n
      jre-lib-path = \"$jrepath\"\n
      dependencies.jar += \"$testpathSrc.jar\"\n
      dependencies.jar += \"$testpathDep-deps.jar\"\n
      entry {\n
       class = \"Ldacapo/$testname/Main\"\n
       method = \"main([Ljava/lang/String;)V\"\n
      }\n
    }\n
    "
    echo -e $contents > $testname.conf
    cd "$root"
}

for test in `ls -d $testroot/sources/*.jar` ; do
    testname=`basename $test`
    testNameNoExtension="${testname%.*}"
    echo -n `basename $testNameNoExtension`...
    createConfigFile $testNameNoExtension
    echo "[DONE]"
done