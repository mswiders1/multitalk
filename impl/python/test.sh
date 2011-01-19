#!/bin/bash

PYTHON=`type -P python`
PYTHON2=`type -P python2`

if [ "x$PYTHON2" == "x" ] ; then
    echo "Run by default python version";
    "$PYTHON" tests.py;
else
    echo "Run by Python 2.x";
    "$PYTHON2" tests.py;
fi;
