var flagName = "popped";

if (widget.getVar(flagName) == null) {
	widget.setVar(flagName, false);	
}

var b = widget.getVar(flagName);

if (PVUtil.getDouble(pvs[0]) > 80) {
		if (b == false) {
			widget.setVar(flagName, true);
			GUIUtil.openWarningDialog("The temperature you set is too high!");
		}
} else if (b == true) {
	widget.setVar(flagName, false);
}
