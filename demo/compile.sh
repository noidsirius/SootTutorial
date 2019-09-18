#!/bin/bash
for DIR in `ls -d */`; do
    echo "Compiling directory $DIR"
    cd $DIR
    rm $(find . -name "*.class") 2> /dev/null
    javac -g $(find . -name "*.java")
    cd ../
done
