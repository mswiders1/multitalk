#!/bin/bash

echo "Kompilacja zasobów"
pyrcc4 ./qtForms/res.qrc -o ./qtForms/res_rc.py
echo "Kompilacja interfejsu użytkownika"
pyuic4 ./qtForms/settings.ui -o ./qtForms/Ui_settings.py
pyuic4 ./qtForms/test.ui -o ./qtForms/Ui_test.py
pyuic4 ./qtForms/window.ui -o ./qtForms/Ui_window.py
pyuic4 ./qtForms/private.ui -o ./qtForms/Ui_private.py
echo "Koniec"

