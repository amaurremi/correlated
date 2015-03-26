#!/bin/bash

# Usage:
# ./configureBenchmarks

### Replace the `jrepath' value with your path to the rt.jar file
jrepath="/usr/lib/jvm/java-7-oracle/jre/lib/rt.jar"

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
createConfigFile db "spec/benchmarks/_209_db/Main" specjvm spec
createConfigFile javac "spec/benchmarks/_213_javac/Main" specjvm spec
createConfigFile jack "spec/benchmarks/_228_jack/Main" specjvm spec
createConfigFile jess "spec/benchmarks/_202_jess/Main" specjvm spec

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
    bmname=$3
    dir=resources/ca/uwaterloo/dataflow/benchmarks/$bmname
    depdir=$4
    mkdir -p $dir
    cd $dir
    testdir=$root/$testroot/$bmname
    if [ -n "$depdir" ]; then
      dep="dependencies.jar += \"$testdir/deps/$depdir.jar\""
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

# format:
# createConfigFileEP <bm name> <entry point regular expression> <benchmark name> <dependency jar name>

# entry point:        spec.benchmarks._200_check.Main.main([Ljava/lang/String;)V
# entry point regexp: spec\.benchmarks\._200_check\.Main\.main\(\[Ljava\/lang\/String\;\)V
# benchmark directory: other
# dependency jar: spec.jar (located under <benchmark dir>/deps)
createConfigFileEP check 'spec\\\\.benchmarks\\\\._200_check\\\\.Main\\\\.main\\\\(\\\\[Ljava\\\\/lang\\\\/String\\\\;\\\\)V' other spec

# entry points:       all methods in package spec.benchmarks._205_raytrace
# entry point regexp: spec\.benchmarks\._205_raytrace\..*
createConfigFileEP raytrace 'spec\\\\.benchmarks\\\\._205_raytrace\\\\.Runner.*' specjvm spec

# no dependency jar
createConfigFileEP scala-library-2.10.2 'scala\\\\.collection\\\\.immutable\\\\.List.*' other

createConfigFileEP mpegaudio 'spec\\\\.benchmarks\\\\._222_mpegaudio\\\\.Main.*' specjvm spec
createConfigFileEP mtrt 'spec\\\\.benchmarks\\\\._205_raytrace\\\\.Runner.*' specjvm spec
