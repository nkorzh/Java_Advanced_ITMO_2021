#!/bin/bash
ROOT=$PWD
LIB_PATH=$PWD/../../../../../../../lib
cd $LIB_PATH
#echo $PWD
#cd $ROOT
javac -cp "$LIB_PATH/junit-4.11.jar." $ROOT/../*.java $ROOT/../tests/*.java

