#!/bin/sh

PYTHON=`type -P python`
PYTHON2=`type -P python2`

if [ x$PYTHON2 == x"" ] ; then
    echo "Run by default python version"
    exec $PYTHON main.py
else
    echo "Run by Python 2.x"
    exec $PYTHON2 main.py
fi;