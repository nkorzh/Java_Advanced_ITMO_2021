#!/bin/bash
EX_DIR="$PWD"
SOL_DIR=$PWD/../../../../..

MODULE_NAME=info.kgeorgiy.ja.kozhuharov.implementor
SEX_PATH="$PWD/../../../../../../../java-advanced-2021"  

MODULE_PATH="$SEX_PATH/artifacts"
LIB_PATH="$SEX_PATH/artifacts:$SEX_PATH/lib"
SRC_FOLDER="info/kgeorgiy/ja/kozhuharov/implementor"

rm -rf $MODULE_PATH/$MODULE_NAME
mkdir $MODULE_PATH/$MODULE_NAME \
  $MODULE_PATH/$MODULE_NAME/info \
  $MODULE_PATH/$MODULE_NAME/info/kgeorgiy \
  $MODULE_PATH/$MODULE_NAME/info/kgeorgiy/ja \
  $MODULE_PATH/$MODULE_NAME/info/kgeorgiy/ja/kozhuharov \
  $MODULE_PATH/$MODULE_NAME/info/kgeorgiy/ja/kozhuharov/implementor

cp -r $SOL_DIR/info/kgeorgiy/ja/kozhuharov/implementor/*.java \
  $MODULE_PATH/$MODULE_NAME/info/kgeorgiy/ja/kozhuharov/implementor
cp $SOL_DIR/module-info.java $MODULE_PATH/$MODULE_NAME/module-info.java
rm -f $MODULE_PATH/$MODULE_NAME/$SRC_FOLDER/module-info.java

javac --module-path $LIB_PATH \
  $MODULE_PATH/$MODULE_NAME/module-info.java \
  $MODULE_PATH/$MODULE_NAME/$SRC_FOLDER/*.java \
  -d $MODULE_PATH/$MODULE_NAME
  
rm -f $MODULE_PATH/$MODULE_NAME/*.java
rm -f $MODULE_PATH/$MODULE_NAME/$SRC_FOLDER/*.java

rm -f $SEX_PATH/artifacts/$MODULE_NAME.jar

jar -c --file=$SEX_PATH/artifacts/$MODULE_NAME.jar \
  --module-path=$LIB_PATH \
  --manifest=$EX_DIR/MANIFEST.MF \
  -C $MODULE_PATH/$MODULE_NAME .
  
rm -rf $MODULE_PATH/$MODULE_NAME
