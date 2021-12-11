from java.lang import System

ok = PVUtil.getDouble(pvs[0])
if ok == 1:
    userName = System.getProperty("UserName")
    password = System.getProperty("Password")
    if userName=="admin" and password == "123456":
        widget.setPropertyValue("visible", True)
    else:
        GUIUtil.showErrorDialog("The user name or password is wrong!")
        pvs[0].setValue(0)
