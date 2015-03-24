#!/bin/bash
sbt "test:runMain ca.uwaterloo.dataflow.correlated.collector.CcBenchmarkRunner $*"