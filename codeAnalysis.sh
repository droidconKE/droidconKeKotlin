#!/bin/bash

./gradlew ktlintFormat && ./gradlew ktlintCheck && ./gradlew detekt && ./gradlew spotlessApply && ./gradlew testDebugUnitTest && ./gradlew createDebugCombinedCoverageReport

# Before use it, in the first time, you must guarantee some running permissions:
# chmod +x codeAnalysis.sh
#
# After that, you just need to run:
# ./codeAnalysis.sh
