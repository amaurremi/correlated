# Correlated Calls Analysis

The goal of this project is to make data flow analysis of Java programs more precise by eliminating infeasible paths through *correlated calls*.
Two method calls are considered correlated if they are invoked on the same receiver object.

The analysis relies on the Inter-procedural Distributive Environment (IDE) data-flow analysis algorithm described in [1].

[1] Sagiv, Mooly, Thomas Reps, and Susan Horwitz. [Precise interprocedural dataflow analysis with applications to constant propagation](http://www.sciencedirect.com/science/article/pii/0304397596000722). Springer Berlin Heidelberg, 1995.

## Project

### Overview

The code of the analysis is written in [Scala](http://www.scala-lang.org/). Our analysis relies on [WALA](http://wala.sourceforge.net/wiki/index.php/Main_Page), a library for static analysis on Java bytecode written in Java. To facilitate the usage of WALA in Scala, we use the [WALAFacade](https://github.com/cos/WALAFacade) library. To be able to add our own modifications to WALAFacade, we included its source code in this project.

Our analysis code is in the `ca.uwaterloo.dataflow` package, whereas the WALAFacade code, with some minor modifications, is in the `edu.illinois.wala` package.

### Testing

#### Run benchmarks
To run the analysis on a benchmark, do the following:
1. Open the shell and type `cd correlated`
2. Type
    - `./eqBench.sh specjvm` to run the *equivalence-IDE* taint analysis on *all* SPEC JVM 98 benchmarks
    - `./eqBench.sh specjvm <benchmark name>` to run the *equivalence-IDE* taint analysis on a *specific* SPEC JVM 98 benchmark, like `db`
    - `./ccBench.sh specjvm` to run the *correlated-calls-IDE* taint analysis on *all* SPEC JVM 98 benchmarks
    - `./ccBench.sh specjvm <benchmark name>` to run the *correlated-calls-IDE* taint analysis on a *specific* SPEC JVM 98 benchmark

To run the analysis on your own benchmark:
1. Put the jar file with the benchmark in the `src/test/scala/ca/uwaterloo/dataflow/benchmarks/other` directory.
2. Enter the entrypoint for the benchmark: if `NAME.jar` is the name of the benchmark file, and `PATH` is the path to the entrypoint class, append the line `createConfigFile NAME PATH other` to `src/test/configureBenchmarks.sh`, analogously to other benchmarks in the file.
3. You can also specify a regular expression as an entrypoint (see examples in `configureBenchmarks.sh`)
4. Navigate to `src/test` and run `./configureBenchmarks`
5. Navigate to the root directory of the project (`~/correlated`) and run `./ccBench.sh other NAME`.

### Run unit tests
To run the unit tests (located in the `src/scala/ca/uwaterloo/dataflow/ide/taint/inputPrograms` folder), run `./unitTests.sh`

