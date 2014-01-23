# Correlated Calls Analysis

The goal of this project is to make data flow analysis of Java programs more precise by eliminating
infeasible paths through correlated calls. Two method calls are considered correlated if they are invoked on the
same receiver object.

## Project

The code of the analysis is written in [Scala](http://www.scala-lang.org/).
Our analysis relies on [WALA](http://wala.sourceforge.net/wiki/index.php/Main_Page), a library for static
analysis on Java bytecode written in Java. To facilitate the usage of WALA in Scala, we use
the [WALAFacade](https://github.com/cos/WALAFacade) library. To be able to add our own modifications to WALAFacade,
we included its source code in this project.

### Set Up

TODO

- in `src.main.resources.application.conf`, adjust the absolute paths of `jre-lib-path` and `dependencies.jar`.