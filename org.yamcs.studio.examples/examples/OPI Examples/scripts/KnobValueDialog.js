var flagName = "popped";

if (widget.getExternalObject(flagName) == null) {
	widget.setExternalObject(flagName, false);	
}

var b = widget.getExternalObject(flagName);

if (PVUtil.getDouble(pvs[0]) > 80) {
		if (b == false) {
			widget.setExternalObject(flagName, true);
			GUIUtil.openWarningDialog("The temperature you set is too high!");
		}
} else if (b == true) {
	widget.setExternalObject(flagName, false);
}
