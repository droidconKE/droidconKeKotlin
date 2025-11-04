@echo off
.\gradlew ktlintFormat && .\gradlew ktlintCheck && .\gradlew detekt && .\gradlew spotlessApply