#!/bin/bash

echo "Compiling java sources"
mvn-hh clean install -f np-array-java/pom.xml

echo "Serializing java arrays"
java -classpath np-array-java/target/test-classes:np-array-java/target/classes ru.hh.search.nparray.GenerateFileTest java_be.data java_le.data

echo "Serializing python arrays"
PYTHONPATH=np-array-python python3 np-array-python/tests/__init__.py python_be.data python_le.data

echo "Comparing results"
cmp -l java_be.data python_be.data || echo "FAILED"
cmp -l java_le.data python_le.data || echo "FAILED"

