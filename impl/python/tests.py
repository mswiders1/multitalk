# -*- coding: utf-8 -*-

import unittest
from tests import Model

def doTests():
    testSuites = [Model.testSuite()]
    all = unittest.TestSuite(testSuites)
    unittest.TextTestRunner(verbosity=2).run(all)

if __name__ == "__main__":
    print("Uruchamiam testy")
    doTests()
