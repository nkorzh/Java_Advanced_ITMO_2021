#!/bin/bash
ROOT=$PWD
LIB_PATH=$PWD/../../../../../../../lib
cd $LIB_PATH
#echo $PWD
#cd $ROOT
javac -encoding UTF8 -cp "$LIB_PATH/junit-4.11.jar." \
  $ROOT/../interfaces/*.java \
  $ROOT/../*.java \
  $ROOT/../tests/*.java \
  $ROOT/../bundles/*.java

