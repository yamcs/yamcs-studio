value = PVUtil.getDouble(pvs[0])

width = 5 * value;
oldY = widget.getPropertyValue("y")
oldHeight = widget.getPropertyValue("height");

widget.setPropertyValue("x", value * 40);
widget.setPropertyValue("y", 500 - width / 2);
widget.setPropertyValue("width", width);
widget.setPropertyValue("height", width);
