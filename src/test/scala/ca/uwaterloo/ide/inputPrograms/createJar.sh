#!/bin/bash

# Usage: ./createJar test_name
# E.g. ./createJar NoCcs

echo $1
rm $1/*.class
rm $1/*.jar
javac $1/*.java
jar cvf $1.jar $1/*.class
mv $1.jar $1
