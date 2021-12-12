var op1 = PVUtil.getDouble(pvs[0]);
var op2 = PVUtil.getDouble(pvs[1]);
var operator = PVUtil.getString(pvs[2]);

var resultPV = pvs[3];

if (operator == "+") {
	resultPV.setValue(op1 + op2);
} else if (operator == "-") {
	resultPV.setValue(op1 - op2);
} else if (operator == "*") {
	resultPV.setValue(op1 * op2);
} else if (operator == "/") {
	resultPV.setValue(op1 / op2);
}
