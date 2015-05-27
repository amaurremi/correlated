#!/bin/bash

# Usage:
# ./configureBenchmarks

### Replace the `jrepath' value with your path to the rt.jar file
jrepath="/Library/Java/JavaVirtualMachines//jdk1.7.0_79.jdk/Contents/Home/jre/lib/rt.jar"

testroot=scala/ca/uwaterloo/dataflow/benchmarks

root=`pwd`
if [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    root="c:/Users/mrapopor/Thesis/project/Correlated Calls/src/test"
fi

function createConfigFile() {
    testname=$1
    entryClass=$2
    benchmarkCollection=$3
    dir=resources/ca/uwaterloo/dataflow/benchmarks/$benchmarkCollection
    mkdir -p $dir
    cd $dir
    testdir=$root/$testroot/$benchmarkCollection
    if [ -n "$4" ]; then
      dep="dependencies.jar += \"$testdir/deps/$4.jar\""
    else
      dep=""
    fi

    contents="
    wala {\n
      jre-lib-path = \"$jrepath\"\n
      dependencies.jar += \"$testdir/$testname.jar\"\n
      $dep\n
      entry {\n
       class = \"L$entryClass\"\n
       method = \"main([Ljava/lang/String;)V\"\n
      }\n
    }\n
    "
    echo -e $contents > $testname.conf
    cd "$root"
}

createConfigFile binarytrees_jython "binarytrees\$py" nonjava
createConfigFile binarytrees_closure "binarytrees/core" nonjava
createConfigFile fasta_groovy "fasta" nonjava
createConfigFile knucleotide_jruby "knucleotide" nonjava
createConfigFile mandelbrot_ocaml "pack/ocamljavaMain" nonjava
createConfigFile mandelbrot_scala "mandelbrot" nonjava
createConfigFile mandelbrot_jython "mandelbrot\$py" nonjava
createConfigFile nbody_scala "nbody" nonjava

createConfigFile check "spec/benchmarks/_200_check/Main" specjvm spec
createConfigFile compress "spec/benchmarks/_201_compress/Main" specjvm spec
createConfigFile raytrace "spec/benchmarks/_205_raytrace/Main" specjvm spec
createConfigFile db "spec/benchmarks/_209_db/Main" specjvm spec
createConfigFile javac "spec/benchmarks/_213_javac/Main" specjvm spec
createConfigFile jack "spec/benchmarks/_228_jack/Main" specjvm spec
createConfigFile jess "spec/benchmarks/_202_jess/Main" specjvm spec
createConfigFile raytrace "spec/benchmarks/_205_raytrace/Main" specjvm spec

function createConfigFileDacapo() {
    testname=$1
    dir="resources/ca/uwaterloo/dataflow/benchmarks/dacapo"
    mkdir -p $dir
    cd $dir
    testdir=$root/$testroot
    testpathSrc=$testdir/dacapo/$testname
    testpathDep=$testdir/dacapo/deps/$testname
    contents="
    wala {\n
      jre-lib-path = \"$jrepath\"\n
      dependencies.jar += \"$testpathSrc.jar\"\n
      dependencies.jar += \"$testpathDep-deps.jar\"\n
      entry {\n
       class = \"Ldacapo/$testname/Main2\"\n
       method = \"main([Ljava/lang/String;)V\"\n
      }\n
    }\n
    "
    echo -e $contents > $testname.conf
    cd "$root"
}

for test in `ls -d $testroot/dacapo/*.jar` ; do
    testname=`basename $test`
    testNameNoExtension="${testname%.*}"
    echo -n `basename $testNameNoExtension`...
    createConfigFileDacapo $testNameNoExtension
    echo "[DONE]"
done

function createConfigFileEP() {
    testname=$1
    entrypoint=$2
    dir=resources/ca/uwaterloo/dataflow/benchmarks/other
    mkdir -p $dir
    cd $dir
    testdir=$root/$testroot/other
    if [ -n "$3" ]; then
      dep="dependencies.jar += \"$testdir/deps/$3.jar\""
    else
      dep=""
    fi
    contents="
    wala {\n
      jre-lib-path = \"$jrepath\"\n
      dependencies.jar += \"$testdir/$testname.jar\"\n
      $dep\n
      entry {\n
       signature-pattern = \"$entrypoint\"\n
      }\n
    }\n
    "
    echo -e $contents > $testname.conf
    cd "$root"
}

# entry point:        spec.benchmarks._200_check.Main.main([Ljava/lang/String;)V
# entry point regexp: spec\.benchmarks\._200_check\.Main\.main\(\[Ljava\/lang\/String\;\)V
createConfigFileEP check 'spec\\\\.benchmarks\\\\._200_check\\\\.Main\\\\.main\\\\(\\\\[Ljava\\\\/lang\\\\/String\\\\;\\\\)V' spec

# entry points:       all methods in package spec.benchmarks._205_raytrace
# entry point regexp: spec\.benchmarks\._205_raytrace\..*
createConfigFileEP raytrace 'spec\\\\.benchmarks\\\\._205_raytrace\\\\.Runner.*' spec

createConfigFileEP scala-library-2.10.2 'scala\\\\.collection\\\\.immutable\\\\.List.*'

#createConfigFileEP java.util-1.7 'java\\\\.util.*'

createConfigFileEP mpegaudio 'spec\\\\.benchmarks\\\\._222_mpegaudio\\\\.Main.*' spec
createConfigFileEP mtrt 'spec\\\\.benchmarks\\\\._205_raytrace\\\\.Runner.*' spec
