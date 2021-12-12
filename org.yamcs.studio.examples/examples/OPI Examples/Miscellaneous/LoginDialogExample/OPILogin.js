var System = Java.type("java.lang.System");

var ok = PVUtil.getDouble(pvs[0]);
if (ok == 1) {
	var username = System.getProperty("UserName");
	var password = System.getProperty("Password");
	if (username == "admin" && password == "123456") {
		widget.setPropertyValue("visible", true);
	} else {
		GUIUtil.openErrorDialog("The user name or password is wrong!");
		pvs[0].setValue(0);
	}
}
