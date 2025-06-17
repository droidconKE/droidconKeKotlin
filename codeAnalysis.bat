@echo off
.\gradlew :multiplatform:ktlintFormat && .\gradlew :multiplatform:ktlintCheck && .\gradlew :multiplatform:detekt && .\gradlew :multiplatform:spotlessApply