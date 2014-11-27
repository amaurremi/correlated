#!/bin/bash

# Usage:
# ./configureBenchmarks

### Replace the `jrepath' value with your path to the rt.jar file
jrepath="/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home/jre/lib/rt.jar"

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

createConfigFile JLex "ca/uwaterloo/dataflow/ide/taint/inputPrograms/JLex/JLex" other

createConfigFile binarytrees_jython "binarytrees\$py" nonjava
createConfigFile binarytrees_closure "binarytrees/core" nonjava
createConfigFile fasta_groovy "fasta" nonjava
createConfigFile knucleotide_jruby "knucleotide" nonjava
createConfigFile mandelbrot_ocaml "pack/ocamljavaMain" nonjava
createConfigFile mandelbrot_scala "mandelbrot" nonjava
createConfigFile mandelbrot_jython "mandelbrot\$py" nonjava
createConfigFile nbody_scala "nbody" nonjava

createConfigFile check "spec/benchmarks/_200_check/Main" specjvm
createConfigFile raytrace "spec/benchmarks/_205_raytrace/Main" specjvm
createConfigFile db "spec/benchmarks/_209_db/Main" specjvm
createConfigFile javac "spec/benchmarks/_213_javac/Main" specjvm
createConfigFile jack "spec/benchmarks/_228_jack/Main" specjvm
createConfigFile jess "spec/benchmarks/_202_jess/Main" specjvm
createConfigFile raytrace "spec/benchmarks/_205_raytrace/Main" specjvm
