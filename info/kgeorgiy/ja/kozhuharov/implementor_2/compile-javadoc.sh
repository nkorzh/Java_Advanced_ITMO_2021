#!/bin/bash
CUR=$PWD
SEX_PATH=$PWD/../../../../../../../java-advanced-2021
TESTS_PATH="$SEX_PATH"/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor
BUILD_PATH="$PWD"/javadoc

rm -rf "$BUILD_PATH"

javadoc \
    -private \
    -link https://docs.oracle.com/en/java/javase/11/docs/api/ \
    -d "$BUILD_PATH" \
    -cp "$SEX_PATH/artifacts/JarImplementorTest.jar: \
     $SEX_PATH/lib/hamcrest-core-1.3.jar: \
     $SEX_PATH/lib/junit-4.11.jar: \
     $SEX_PATH/lib/jsoup-1.8.1.jar: \
     $SEX_PATH/lib/quickcheck-0.6.jar:" \
     "$PWD"/CodeGenerator.java \
     "$PWD"/Implementor.java \
     "$TESTS_PATH"/Impler.java \
     "$TESTS_PATH"/JarImpler.java \
     "$TESTS_PATH"/ImplerException.java 
