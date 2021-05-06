#!/bin/bash
SEX_PATH="$PWD/../../../../../../../java-advanced-2021"
LIB="$SEX_PATH/artifacts:$SEX_PATH/lib"
OUT_FILE=$SEX_PATH/artifacts/LdapReferralExceptionImpl.jar
rm -f $OUT_FILE 
java --module-path="$LIB" \
  -m info.kgeorgiy.ja.kozhuharov.implementor/info.kgeorgiy.ja.kozhuharov.implementor.Implementor \
  --jar info.kgeorgiy.java.advanced.implementor.full.classes.standard.LdapReferralException $OUT_FILE