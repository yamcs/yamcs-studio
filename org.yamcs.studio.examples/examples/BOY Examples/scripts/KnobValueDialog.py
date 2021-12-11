#module in the same directory is visible to this script
import WidgetUtil

GREEN = ColorFontUtil.getColorFromRGB(0, 180, 0)
RED = ColorFontUtil.RED

#Name of the flag to show if dialog has been popped up.
flagName = "popped"

labelName = "myLabel"

if widget.getExternalObject(flagName) == None:
    widget.setExternalObject(flagName, 0)    
    #Example to write text to Console View
    ConsoleUtil.writeInfo("Welcome to OPI Examples!")

b = widget.getExternalObject(flagName);

if PVUtil.getDouble(pvs[0]) > PVUtil.getDouble(pvs[1]):  
        s = "Temperature is too high!"
        WidgetUtil.setText(display, labelName, s)
        WidgetUtil.setForeColor(display, labelName, RED)
        #If dialog has not been popped up, pop up the dialog
        if b == 0:
            #set popped flag to true
            widget.setExternalObject(flagName, 1)
            GUIUtil.showWarningDialog("The temperature you set is too high!")
else:
    s = "Temperature is normal"
    WidgetUtil.setText(display, "myLabel", s)
    WidgetUtil.setForeColor(display, labelName, GREEN)
    #reset popped flag to false
    if b != 0:
        widget.setExternalObject(flagName, 0)
