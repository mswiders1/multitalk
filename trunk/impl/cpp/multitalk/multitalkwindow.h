#ifndef MULTITALKWINDOW_H
#define MULTITALKWINDOW_H

#include <QMainWindow>
#include <QLabel>
#include "connectdialog.h"

namespace Ui {
    class MultitalkWindow;
}

class MultitalkWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MultitalkWindow(QWidget *parent = 0);
    ~MultitalkWindow();

private:
    Ui::MultitalkWindow *ui;
    QLabel* statusBarLabel;


private slots:
    void connectToNetwork();
};

#endif // MULTITALKWINDOW_H
