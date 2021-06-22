#set echo off
ROOT=$PWD
LIB_PATH=$PWD/../../../../../../../lib
#set echo on

javac -cp "$LIB_PATH/junit-4.11.jar." $ROOT/../*.java
rmiregistry.exe
