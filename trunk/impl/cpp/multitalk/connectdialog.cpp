#include "connectdialog.h"
#include "ui_connectdialog.h"
#include <QDebug>

ConnectDialog::ConnectDialog(QWidget *parent) :
    QDialog(parent),
    ui(new Ui::ConnectDialog)
{
    ui->setupUi(this);
    ui->nickLineEdit->setText("maciek");
    connect(ui->buttonBox,SIGNAL(accepted()),this,SLOT(okClicked()));
}

ConnectDialog::~ConnectDialog()
{
    delete ui;
}

void ConnectDialog::okClicked()
{
    emit ipChanged(ui->ipLineEdit->text());
    emit nickChanged(ui->nickLineEdit->text());
}
