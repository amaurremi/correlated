# Correlated Calls Analysis

The goal of this project is to make data flow analysis of Java programs more precise by eliminating infeasible paths through *correlated calls*.
Two method calls are considered correlated if they are invoked on the same receiver object.

The analysis relies on the Inter-procedural Distributive Environment (IDE) data-flow analysis algorithm described in [1].

[1] Sagiv, Mooly, Thomas Reps, and Susan Horwitz. [Precise interprocedural dataflow analysis with applications to constant propagation](http://www.sciencedirect.com/science/article/pii/0304397596000722). Springer Berlin Heidelberg, 1995.

## Project

### Overview

The code of the analysis is written in [Scala](http://www.scala-lang.org/). Our analysis relies on [WALA](http://wala.sourceforge.net/wiki/index.php/Main_Page), a library for static analysis on Java bytecode written in Java. To facilitate the usage of WALA in Scala, we use the [WALAFacade](https://github.com/cos/WALAFacade) library. To be able to add our own modifications to WALAFacade, we included its source code in this project.

Thus, our analysis code is in the `ca.uwaterloo.dataflow` package, whereas the WALAFacade code, with some minor modifications, is in the `edu.illinois.wala` package.

### Set Up

1. Install the WALA library into your local [Maven](http://maven.apache.org/) repository, as described in the [first](https://github.com/cos/WALAFacade#steps) step of the WALAFacade installation instructions.

2. Build the project with [SBT](http://www.scala-sbt.org/): 
  - Checkout the Correlated Calls project and navigate into its directory.
  - [Install](http://www.scala-sbt.org/release/docs/Getting-Started/Setup) SBT on your machine.
  - Navigate into the checked out Correlated Calls project directory from the command line.
    - `sbt gen-idea`, if you'd like to use [IntelliJ IDEA](http://www.jetbrains.com/idea/),
    - `sbt eclipse`, if you'd like to use [Eclipse](http://www.eclipse.org/),
    - `sbt`, if you prefer using another IDE. From the SBT shell, you'll need to type `compile` to compile the project, and `test` to run the tests.

### Testing
The analysis consumes input Java programs in the form of JAR files. The source code of the input programs is located in the `src/test/scala/ca/uwaterloo/taint/inputPrograms` directory.
Additionally, each test program needs a configuration file in the `src/test/resources` directory.

To create or update a configuration and Jar file for a test, you will need to execute the `configureTests` or `configureSingleTest` script.
The first script will configure all tests, and the latter script will configure one specified test.

1. In both files `configureTests` and `configureSingleTest`, replace the value of the `jrepath` variable with the path to the `rt.jar` (or classes.jar on OS X) file.
The `rt.jar` file contains Java's bootstrap classes and is usually located in Java's `jre/lib` directory. On a Mac, the path might look like `/System/Library/Frameworks/JavaVM.framework/Classes/classes.jar`.
2. To configure all tests, navigate to the `src/test` subdirectory of the project and run `./configureTests`.
3. To configure a single test, navigate to the `src/test` subdirectory of the project and run `./configureSingleTest <test name>`.
   For example, to configure the test `ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionCall.FunctionCall.java`, run
```
./configureSingleTest FunctionCall
```
