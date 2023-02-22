#!/bin/sh

echo "Running static analysis."

./codeAnalysis.sh
./gradlew clean test