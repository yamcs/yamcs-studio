var flagName = "firstRun";

// Avoid running this script if the script is triggered during opi startup
if (widgetController.getVar(flagName) == null) {
	widgetController.setVar(flagName, false);
} else {
	var macroInput = DataUtil.createMacrosInput(true);
	macroInput.put("pv", PVUtil.getString(pvArray[0]));
	
	// Open an OPI with the new Macro Input
	ScriptUtil.openOPI(widgetController, "embeddedOPI.opi", 0, macroInput);
}
