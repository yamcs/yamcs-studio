"""
Initialize the calculator model.
"""

from CalculatorModel import CalculatorModel

'''Create a calculator model and add it to display as a var.'''
display.setVar("calc", CalculatorModel(display.getWidget("LCD")))
