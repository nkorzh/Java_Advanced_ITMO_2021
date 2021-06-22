#!/bin/bash
ROOT=$PWD
LIB_PATH=$PWD/../../../../../../../lib
chmod u+x compile.sh
bash compile.sh || exit
cd $ROOT/../ || exit

java -cp "$LIB_PATH/junit-4.11.jar:$LIB_PATH/hamcrest-core-1.3.jar:$LIB_PATH/../java-solutions/" \
  org.junit.runner.JUnitCore \
  info.kgeorgiy.ja.kozhuharov.i18n.tests.EnglishTextTest
    
java -cp "$LIB_PATH/junit-4.11.jar:$LIB_PATH/hamcrest-core-1.3.jar:$LIB_PATH/../java-solutions/" \
  org.junit.runner.JUnitCore \
  info.kgeorgiy.ja.kozhuharov.i18n.tests.RussianTextTest
  
  
cd $ROOT
bash clean.sh
