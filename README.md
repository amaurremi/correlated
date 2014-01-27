# Correlated Calls Analysis

The goal of this project is to make data flow analysis of Java programs more precise by eliminating infeasible paths through *correlated calls*.
Two method calls are considered correlated if they are invoked on the same receiver object.

## Project

### Overview

The code of the analysis is written in [Scala](http://www.scala-lang.org/). Our analysis relies on [WALA](http://wala.sourceforge.net/wiki/index.php/Main_Page), a library for static analysis on Java bytecode written in Java. To facilitate the usage of WALA in Scala, we use the [WALAFacade](https://github.com/cos/WALAFacade) library. To be able to add our own modifications to WALAFacade, we included its source code in this project.

Thus, our analysis code is in the `ca.uwaterloo.correlated` package, whereas the WALAFacade code, with some minor modifications, is in the `edu.illinois.wala` package.

### Set Up

1. Install the WALA library into your local [Maven](http://maven.apache.org/) repository, as described in the [first](https://github.com/cos/WALAFacade#steps) step of the WALAFacade installation instructions.

2. Build the project with [SBT](http://www.scala-sbt.org/): 
  - [Install](http://www.scala-sbt.org/release/docs/Getting-Started/Setup) SBT on your machine.
  - Navigate into the checked out Correlated Calls project directory from the command line and run
    - `sbt gen-idea`, if you'd like to use [IntelliJ IDEA](http://www.jetbrains.com/idea/),
    - `sbt eclipse`, if you'd like to use [Eclipse](http://www.eclipse.org/),
    - `sbt`, if you prefer using another IDE. From the SBT shell, type `compile` to compile the project, and `test` to run the tests.

**Note**: for now, to run tests, it's necessary to adjust the absolute paths in the `.conf` files of the `src.main.resources` directory.