# Correlated Calls Analysis

The goal of this project is to make data flow analysis of Java programs more precise by eliminating
infeasible paths through correlated calls. Two method calls are considered correlated if they are invoked on the
same receiver object.

## Project

### Overview

The code of the analysis is written in [Scala](http://www.scala-lang.org/).
Our analysis relies on [WALA](http://wala.sourceforge.net/wiki/index.php/Main_Page), a library for static
analysis on Java bytecode written in Java. To facilitate the usage of WALA in Scala, we use
the [WALAFacade](https://github.com/cos/WALAFacade) library. To be able to add our own modifications to WALAFacade,
we included its source code in this project.

### Set Up

The project can be built with [SBT](http://www.scala-sbt.org/). To do that, first,
[install](http://www.scala-sbt.org/release/docs/Getting-Started/Setup) SBT on your machine.
Then navigate into the checked out Correlated Calls project directory from the command line and run
- `sbt gen-idea`, if you'd like to use [IntelliJ IDEA](http://www.jetbrains.com/idea/)
- `sbt eclipse`, if you'd like to use [Eclipse](http://www.eclipse.org/)
- `sbt`, if you prefer using another IDE. From the SBT shell, type `compile` to compile the project, `run` to run it, and `test` to run the tests.

To run tests, first adjust the absolute paths of `jre-lib-path` and `dependencies.jar`
in the `src.main.resources.application.conf` file.