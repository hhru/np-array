#!/bin/bash

echo "Compiling java sources"
mvn-hh clean install -f np-array-java/pom.xml

echo "Serializing java arrays"
java -classpath np-array-java/target/test-classes:np-array-java/target/classes ru.hh.search.nparray.GenerateFileTest java.data

echo "Serializing python arrays"
PYTHONPATH=np-array-python python3 np-array-python/tests/__init__.py python.data

echo "Comparing results"
cmp -l java.data python.data || echo "FAILED"
