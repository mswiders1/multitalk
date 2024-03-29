#-------------------------------------------------
#
# Project created by QtCreator 2011-01-14T18:46:50
#
#-------------------------------------------------

QT       += core gui network

TARGET = multitalk
TEMPLATE = app


SOURCES += main.cpp\
        multitalkwindow.cpp \
    connectdialog.cpp \
    broadcast.cpp \
    tcpserver.cpp \
    tcpconnection.cpp \
    userdata.cpp \
    message.cpp

HEADERS  += multitalkwindow.h \
    connectdialog.h \
    broadcast.h \
    tcpserver.h \
    tcpconnection.h \
    userdata.h \
    message.h

FORMS    += multitalkwindow.ui \
    connectdialog.ui

LIBS += -lqjson
