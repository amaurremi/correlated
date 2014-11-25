#!/bin/bash

# Usage:
# ./configureBenchmarks

### Replace the `jrepath' value with your path to the rt.jar file
jrepath="/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home/jre/lib/rt.jar"

testroot=scala/ca/uwaterloo/dataflow/benchmarks/other

root=`pwd`
if [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    root="c:/Users/mrapopor/Thesis/project/Correlated Calls/src/test"
fi

function createConfigFile() {
    testname=$1
    entryClass=$2
    dir="resources/ca/uwaterloo/dataflow/benchmarks/other"
    mkdir -p $dir
    cd $dir
    testdir=$root/$testroot
    contents="
    wala {\n
      jre-lib-path = \"$jrepath\"\n
      dependencies.jar += \"$testdir/$testname.jar\"\n
      entry {\n
       class = \"L$entryClass\"\n
       method = \"main([Ljava/lang/String;)V\"\n
      }\n
    }\n
    "
    echo -e $contents > $testname.conf
    cd "$root"
}

createConfigFile JLex "ca/uwaterloo/dataflow/ide/taint/inputPrograms/JLex/JLex"
