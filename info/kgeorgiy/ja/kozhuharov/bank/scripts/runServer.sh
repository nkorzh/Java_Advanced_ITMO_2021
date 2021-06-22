#!/bin/bash

cd "$PWD/../../../../../../" || exit

rmiregistry & 
sleep 1
java info.kgeorgiy.ja.kozhuharov.bank.Server

killall rmiregistry