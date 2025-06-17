#!/bin/bash

./gradlew :multiplatform:ktlintFormat && ./gradlew :multiplatform:ktlintCheck && ./gradlew :multiplatform:detekt && ./gradlew :multiplatform:spotlessApply

# Before use it, in the first time, you must guarantee some running permissions:
# chmod +x codeAnalysis.sh
#
# After that, you just need to run:
# ./codeAnalysis.sh
