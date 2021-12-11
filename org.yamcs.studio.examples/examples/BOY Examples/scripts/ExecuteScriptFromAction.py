from java.lang import Math

if Math.random() > 0.5:
	color = ColorFontUtil.getColorFromRGB(0,160,0)
	colorName = "green"

else:
	color = ColorFontUtil.RED
	colorName = "red"

import WidgetUtil
WidgetUtil.setBackColor(display, "myIndicator", color)
WidgetUtil.setMyForeColor(widget, color)

GUIUtil.openInformationDialog("Python says: my color is " + colorName)
