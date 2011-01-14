#include <QtGui/QApplication>
#include "multitalkwindow.h"

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    MultitalkWindow w;
    w.show();

    return a.exec();
}
