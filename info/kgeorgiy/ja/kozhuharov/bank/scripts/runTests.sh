#!/bin/bash
ROOT=$PWD
LIB_PATH=$PWD/../../../../../../../lib
chmod u+x compile.sh
bash compile.sh || exit
cd $ROOT/../ || exit

java -cp "$LIB_PATH/junit-4.11.jar:$LIB_PATH/hamcrest-core-1.3.jar:$LIB_PATH/../java-solutions/" \
  info.kgeorgiy.ja.kozhuharov.bank.tests.BankTester &> tmpfile
echo "$?"
rm -f tmpfile
cd $ROOT
bash clean.sh


