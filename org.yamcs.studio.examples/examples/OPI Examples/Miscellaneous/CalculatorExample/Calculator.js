var CalculatorModel = function() {
	return {
		oper1: "0",
		oper2: "0",
		operator: undefined,
		lcd: display.getWidget("LCD"),
		setLCDText: function(text) {
			this.lcd.setPropertyValue("text", text);
		},
		appendDigit: function(digit) {
			var o;
			if (this.operator === undefined) {
				o = this.oper1;
			} else {
				o = this.oper2;
			}
			
			o = ((o == "0") ? "" : o) + digit;
			if (this.operator == undefined) {
				this.oper1 = o;
			} else {
				this.oper2 = o;
			}
	
			this.setLCDText(o);
		},
		setOperator: function(operator) {
			this.oper1 = this.lcd.getPropertyValue("text");
			this.operator = operator;
		},
		calc: function() {
			var r;
			if (this.operator == "+") {
				r = Number(this.oper1) + Number(this.oper2);
			} else if (this.operator == "-") {
				r = Number(this.oper1) - Number(this.oper2);
			} else if (this.operator == "*") {
				r = Number(this.oper1) * Number(this.oper2);
			} else if (this.operator == "/") {
				r = Number(this.oper1) / Number(this.oper2);
			}
			this.operator = undefined;
			this.oper1 = '0';
			this.oper2 = '0';
			if (r != undefined) {
				this.setLCDText('' + r);
			}
		}
	};
};

display.setVar("calc", CalculatorModel())
