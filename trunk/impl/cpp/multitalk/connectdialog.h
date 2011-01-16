#ifndef CONNECTDIALOG_H
#define CONNECTDIALOG_H

#include <QDialog>

namespace Ui {
    class ConnectDialog;
}

class ConnectDialog : public QDialog
{
    Q_OBJECT

public:
    explicit ConnectDialog(QWidget *parent = 0);
    ~ConnectDialog();
    QString ip;
    QString nick;

private:
    Ui::ConnectDialog *ui;

signals:
    void ipChanged(QString ip);
    void nickChanged(QString nick);

private slots:
    void okClicked();
};

#endif // CONNECTDIALOG_H
