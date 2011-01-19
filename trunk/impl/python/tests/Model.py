from model.Model import Model
from gui.Main import Main
import random
import unittest

class TestModelMatrix(unittest.TestCase):

    def setUp(self):
        self.gui = Main()
        self.model = Model()
        self.model.setGui(self.gui)
        self.gui.setModel(self.model)
        self.model.addNode('2jmj7l5rSw0yVb/vlWAYkK/YBwk=', 'adsa', '192.168.0.1')
        self.model.addNode('2jmj7l5rSw0yVb/vlWAYkK/YCwk=',  'sss', '192.168.0.2')
        self.model.addNode('2jmj7l5rSw0yVb/vlWAYkK/YDwk=',  'ccc', '192.168.0.3')

    def test_emptyMatrix(self):
        row1 = [1,  2,  3]
        row2 = [4,  5,  3]
        row3 = [0,  0,  0]
        matrix = [row1,  row2,  row3]
        vec = ["jeden",  "dwa",  "trzy"]
        
        self.model.addMatrix(matrix,  vec)
            
    def test_theSameMatrix(self):
        self.test_emptyMatrix()
        self.test_emptyMatrix()
            
def testSuite():
    matrixTestSuit = unittest.TestLoader().loadTestsFromTestCase(TestModelMatrix)
    return matrixTestSuit
