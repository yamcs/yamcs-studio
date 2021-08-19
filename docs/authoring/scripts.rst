Scripts
=======

For more advanced dynamic runtime behaviour, we can write scripts (actually Rules are a thin layer on top of scripts). With scripts we can write arbitrary logic that can dynamically manipulate just about any combination of properties for a widget.

Yamcs Studio supports two dynamic languages: JavaScript and Python. Both languages can be used to the same effect, and are available without any external dependencies. As of now, there is no advanced editor support bundled with Yamcs Studio though, so scripts are edited with a plain text editor.

Additional, both from JavaScript and Python scripts, Java code can called from within the script by importing the desired Java packages.


Create a script for a widget
----------------------------

#. Edit the **Scripts** property to pop up this dialog.

   .. image:: _images/attach-scripts.png
       :alt: Attach a Script
       :align: center

#. Clicking the plus icon gives allows to select one or more scripts that will be executed. A script definition can be embedded in the display which is an acceptable solution for simple scripts. Else scripts are defined in their own javascript or python file, stored in the project workspace.

#. In the In the right **Input PVs** table add your input PV(s). The script(s) will be triggered when the PV receives an udpate. At least one PV must be set as **Trigger** for the script to be executed.

#. The **Options** tab provides some options for specific cases:

   * **Skip executes triggered by PVs' first value.** will prevent the execution of the sript on the first update of the PVs.
   * **Execute anyway even if some PVs are diconnected**: by default the scripts are not executed if some of the input PVs are disconnected.
   * **Do not execute the script if erro was detected**: by default if an execution of the script fails, the scrip is executed again when a PV update is received.


Access Widgets
--------------

To access and control the widgets of the OPI displays, two special objects are defined by the CSStudio layer: **widget** and **display**.


.. rubric:: widget object

The widget to which a script is attached can be accessed in the script via widget object. Widget object provides the
methods to get or set any of its properties, store external objects or provide special methods for a particular widget.


.. rubric:: display object

The widget controller of the display is accessible in all scripts as display object. To get the controller of any
widget in the display, you can call its method getWidget(name). For example:

.. code:: javascript

    display.getWidget("myLabel").setPropertyValue("x", 20); //set x position of the widget myLabel to 20.


.. rubric:: Common methods to all widgets

* ``getPropertyValue(prop_id)``
* ``setPropertyValue(prop_id, value)``
* ``setPropertyValue(prop_id, value, forceFire)``
* ``getPVByName(pvName)``
* ``setVar(varName, varValue)``
* ``getVar(name)``
* ``getMacroValue(macroName)``
* ``executeAction(index)``
* ``setEnabled(enable)``
* ``setVisible(visible)``
* ``setX(x)``
* ``setY(y)``
* ``setWidth(width)``
* ``setHeight(height)``


.. rubric:: Common methods to all container widgets

Container widgets includes Display, Grouping Container, Linking Container and Tabbed Container.

* ``getChild(name)``
* ``getWidget(name)``
* ``addChild(model)``
* ``addChildToRight(model)``
* ``addChildToBottom(model)``
* ``removeChildByName(name)``
* ``removeChild(widget)``
* ``removeChild(index)``
* ``removeAllChildren()``
* ``performAutosize()``
* ``getValue()``
* ``setValue()``


.. rubric:: Common methods to all PV widgets

Any widget that has PV Name property is PV widget. For example, Text Update, Combo Box, XY Graph and so on.

* ``getPV()``
* ``getPV(pvPropId)``
* ``getValue()``
* ``setValue(value)``
* ``setValueInUIThread(value)``


Access PV
---------

.. rubric:: pvs object

The input PVs for a script can be accessed in script via pvs object. The order of the input PVs in the configuation
list is preserved in pvs. pvs[0] is the first input pv. If you have N input PVs, pvs[N-1] is the last input PV.

You may also able to get the PV attached to a PV widget via ``widget.getPV()``.

In script, you can read/write PV or get its timestamp or severity via the utility APIs provided in PVUtil


.. rubric:: triggerPV object

The PV that triggers the execution of the script can be accessed via triggerPV object. When there are more than one
trigger PV for a script and you need to know this execution is triggered by which PV, you can use this object. For
example:

.. code:: javascript

    if (triggerPV === pvs[1]) {
        ConsoleUtil.writeInfo("I'm triggered by the second input PV.");
    }


.. rubric:: Examples

#. Get double value from PV

   .. code:: javascript

       var value = PVUtil.getDouble(pvs[0]);
       widget.setPropertyValue("start_angle", value);

#. Write PV Value

   If writing a PV is forbidden by PV security, an exception will be thrown and shown in console. The method ``PV.setValue(data)`` accepts Double, Double[], Integer, String.

   .. code:: javascript

       pvs[0].setValue(0);

#. Get severity of PV

   .. code:: javascript

       var RED = ColorFontUtil.RED;
       var ORANGE = ColorFontUtil.getColorFromRGB(255,255,0);
       var GREEN = ColorFontUtil.getColorFromHSB(120.0,1.0,1.0);
       var PINK = ColorFontUtil.PIN
       var severity = PVUtil.getSeverity(pvs[0]);
       var colo
       switch(severity){
           case 0:  //OK
               color = GREEN;
               break;
           case 1:  //MAJOR
               color = RED;
               break;
           case 2:  //MINOR
               color = ORANGE;
               break;
           case -1:  //INVALID
           default:
               color = PINK;
       }
       widget.setPropertyValue("foreground_color", color);


Scripts Default Packages
------------------------

To ease the implementation of scripts, a number of APIs are provided in the packages *org.csstudio.opibuilder.scriptUtil* and *org.yamcs.studio.script*. Those packages are imported by default in each stript and there is no need to import them at the begining of a script.

This section lists the methods available in the package *org.yamcs.studio.script* via the following class:

* Yamcs

via the package *org.csstudio.opibuilder.scriptUtil*, provided by the CSStudio layer, in the following classes:

* PVUtil
* ColorFontUtil
* DataUtil
* ScriptUtil
* FileUtil
* WidgetUtil
* GUIUtil
* ConsoleUtil


Yamcs
^^^^^

.. rubric:: Method Summary

.. list-table::
    :widths: 25 75

    * - Type
      - Method and Description
    * - ``void``
      - | ``issueCommand(java.lang.String commandSource)``
        | Issue a telecommand via the current Yamcs instance. Example:
        |     ``Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON(voltage_num: 1)')``


PVUtil
^^^^^^

The utility class to facilitate Javascript programming for PV operation.

.. rubric:: Method Summary

.. list-table::
    :widths: 25 75

    * - Type
      - Method and Description
    * - ``org.yamcs.studio.data.IPV``
      - | ``createPV(java.lang.String name, org.csstudio.opibuilder.editparts.AbstractBaseEditPart widget)``
        | Create a PV and start it.
    * - ``double``
      - | ``getDouble(org.yamcs.studio.data.IPV pv)``
        | Try to get a double number from the PV.
    * - ``double``
      - | ``getDouble(org.yamcs.studio.data.IPV pv, int index)``
        | Try to get a double-typed array element from the Value.
    * - ``double[]``
      - | ``getDoubleArray(org.yamcs.studio.data.IPV pv)``
        | Try to get a double-typed array from the pv.
    * - ``java.lang.String``
      - | ``getFullString(org.yamcs.studio.data.IPV pv)``
        | Get the full info from the pv in this format
    * - ``java.lang.String[]``
      - | ``getLabels(org.yamcs.studio.data.IPV pv)``
        | Get the list of Enum values
    * - ``java.lang.Long``
      - | ``getLong(org.yamcs.studio.data.IPV pv)``
        | Try to get a long integer number from the PV.
    * - ``long[]``
      - | ``getLongArray(org.yamcs.studio.data.IPV pv)``
        | Try to get an integer-typed array from the pv.
    * - ``int``
      - | ``getSeverity(org.yamcs.studio.data.IPV pv)``
        | Get severity of the pv as an integer value.
    * - ``java.lang.String``
      - | ``getSeverityString(org.yamcs.studio.data.IPV pv)``
        | Get severity of the PV as a string.
    * - ``double``
      - | ``getSize(org.yamcs.studio.data.IPV pv)``
        | Get the size of the pv's value
    * - ``java.lang.String``
      - | ``getStatus(org.yamcs.studio.data.IPV pv)``
        | Get the status text that might describe the severity.
    * - ``java.lang.String``
      - | ``getString(org.yamcs.studio.data.IPV pv)``
        | Converts the given pv's value into a string representation.
    * - ``java.lang.String[]``
      - | ``getStringArray(org.yamcs.studio.data.IPV pv)``
        | Get string array from pv.
    * - ``double``
      - | ``getTimeInMilliseconds(org.yamcs.studio.data.IPV pv)``
        | Get milliseconds since epoch.
    * - ``java.lang.String``
      - | ``getTimeString(org.yamcs.studio.data.IPV pv)``
        | Get the timestamp string of the pv
    * - ``java.lang.String``
      - | ``getTimeString(org.yamcs.studio.data.IPV pv, java.lang.String formatPattern)``
        | Get the timestamp string of the pv
    * - ``void``
      - | ``writePV(java.lang.String pvName, java.lang.Object value)``
        | Write a PV in a background job.
    * - ``void``
      - | ``writePV(java.lang.String pvName, java.lang.Object value, int timeout)``
        | Write a PV in a background job.


.. _ColorFontUtil:

ColorFontUtil
^^^^^^^^^^^^^

Utility class to facilitate Javascript programming for color operation.

.. rubric:: Field Summary

.. list-table::
    :widths: 25 75

    * - Type
      - Method and Description
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``BLACK``
        | the color black
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``BLUE``
        | the color blue
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``CYAN``
        | the color cyan
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``DARK_GRAY``
        | the color dark gray
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``GRAY``
        | the color gray
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``GREEN``
        | the color green
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``LIGHT_BLUE``
        | the color light blue
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``ORANGE``
        | the color orange
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``PINK``
        | the color pink
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``PURPLE``
        | the color purple
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``RED``
        | the color red
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``WHITE``
        | the color white
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``YELLOW``
        | the color yellow

.. rubric:: Method Summary

.. list-table::
    :widths: 25 75

    * - Type
      - Method and Description
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``getColorFromHSB(float hue, float saturation, float brightness)``
        | Get a color with the given hue, saturation, and brightness.
    * - ``org.eclipse.swt.graphics.RGB``
      - | ``getColorFromRGB(int red, int green, int blue)``
        | Get a color with the given red, green and blue values.
    * - ``org.eclipse.swt.graphics.FontData``
      - | ``getFont(java.lang.String name, int height, int style)``
        | Get a new font data given a font name, the height of the desired font in points, and a font style.


DataUtil
^^^^^^^^

Utility class to facilitate Javascript programming for data operation.

.. rubric:: Method Summary

.. list-table::
    :widths: 25 75

    * - Type
      - Method and Description
    * - ``double[]``
      - | ``createDoubleArray(int size)``
        | Create a new double array with given size.
    * - ``int[]``
      - | ``createIntArray(int size)``
        | Create a new int array with given size.
    * - ``org.csstudio.opibuilder.util.MacrosInput``
      - | ``createMacrosInput(boolean include_parent_macros)``
        | Create a MacrosInput, which can be used as the macros input for a container widget or display.
    * - ``double[]``
      - ``toJavaDoubleArray(org.mozilla.javascript.NativeArray jsArray)``
        | Convert JavaScript array to Java double array.
    * - ``int[]``
      - | ``toJavaIntArray(org.mozilla.javascript.NativeArray jsArray)``
        | Convert JavaScript array to Java int array.


ScriptUtil
^^^^^^^^^^

The utility class to facilitate OPI script programming.

.. rubric:: Method Summary

.. list-table::
    :widths: 25 75

    * - Type
      - Method and Description
    * - ``void``
      - | ``closeCurrentOPI()``
        | Close current active OPI.
    * - ``void``
      - | ``execInUI(java.lang.Runnable runnable,org.csstudio.opibuilder.editparts.AbstractBaseEditPart widget)``
        | Execute a runnable in UI thread.
    * - ``void``
      - | ``executeEclipseCommand(java.lang.String commandId)``
        | Execute an Eclipse command.
    * - ``void``
      - | ``executeSystemCommand(java.lang.String command,int wait)``
        | Executing a system or shell command.
    * - ``org.osgi.framework.Version``
      - ``getBOYVersion()``
    * - ``void``
      - | ``openOPI(org.csstudio.opibuilder.editparts.AbstractBaseEditPart widget, java.lang.String opiPath, int target, org.csstudio.opibuilder.util.MacrosInput macrosInput)``
        | Open an OPI.


FileUtil
^^^^^^^^

The Utility class to help file operating.

.. rubric:: Method Summary

.. list-table::
    :widths: 25 75

    * - Type
      - Method and Description
    * - ``java.io.InputStream``
      - | ``getInputStreamFromFile(java.lang.String filePath, org.csstudio.opibuilder.editparts.AbstractBaseEditPart widget)``
        | Return an ``InputStream`` of the file on the specified path.
    * - ``org.jdom.Element``
      - | ``loadXMLFile(java.lang.String filePath)``
        | Load the root element of an XML file.
    * - ``org.jdom.Element``
      - | ``loadXMLFile(java.lang.String filePath, org.csstudio.opibuilder.editparts.AbstractBaseEditPart widget)``
        | Load the root element of an XML file.
    * - ``void``
      - | ``openFile(java.lang.String filePath, org.csstudio.opibuilder.editparts.AbstractBaseEditPart widget)``
        | Open a file in default editor.
    * - ``java.lang.String``
      - | ``openFileDialog(boolean inWorkspace)``
        | Open a file select dialog.
    * - ``void``
      - | ``openWebPage(java.lang.String link)``
        | Open a web page.
    * - ``void``
      - | ``playWavFile(java.lang.String filePath, org.csstudio.opibuilder.editparts.AbstractBaseEditPart widget)``
        | Play a .wav file.
    * - ``java.lang.String``
      - | ``readTextFile(java.lang.String filePath)``
        | Read a text file.
    * - ``java.lang.String``
      - | ``readTextFile(java.lang.String filePath, org.csstudio.opibuilder.editparts.AbstractBaseEditPart widget)``
        | Read a text file.
    * - ``java.lang.String``
      - | ``saveFileDialog(boolean inWorkspace)``
        | Open a file save dialog.
    * - ``java.lang.String``
      - | ``workspacePathToSysPath(java.lang.String workspacePath)``
        | Convert a workspace path to system path.
    * - ``void``
      - | ``writeTextFile(java.lang.String filePath, boolean inWorkspace, org.csstudio.opibuilder.editparts.AbstractBaseEditPart widget, java.lang.String text, boolean append)``
        | Write a text file.
    * - ``void``
      - | ``writeTextFile(java.lang.String filePath, boolean inWorkspace, java.lang.String text, boolean append)``
        | Write a text file.


WidgetUtil
^^^^^^^^^^

The Utility Class to help managing widgets.

.. rubric:: Method Summary

.. list-table::
    :widths: 25 75

    * - Type
      - Method and Description
    * - ``org.csstudio.opibuilder.model.AbstractWidgetModel``
      - | ``createWidgetModel(java.lang.String widgetTypeID)``
        | Create a new widget model with the give widget type ID.


GUIUtil
^^^^^^^

The utility class to facilitate script programming in GUI operation.

.. rubric:: Method Summary

.. list-table::
    :widths: 25 75

    * - Type
      - Method and Description
    * - ``void``
      - | ``compactMode()``
        | Enter or exit compact mode.
    * - ``void``
      - | ``fullScreen()``
        | Enter or exit full screen.
    * - ``boolean``
      - | ``openConfirmDialog(java.lang.String dialogMessage)``
        | Open a dialog to ask for confirmation.
    * - ``boolean``
      - | ``openPasswordDialog(java.lang.String dialogMessage, java.lang.String password)``
        | Open a password dialog to allow user to input password.


ConsoleUtil
^^^^^^^^^^^

The Utility Class to help write information to CSS console.

.. rubric:: Method Summary

.. list-table::
    :widths: 25 75

    * - Type
      - Method and Description
    * - ``void``
      - | ``writeError(java.lang.String message)``
        | Write Error information to CSS console.
    * - ``void``
      - | ``writeInfo(java.lang.String message)``
        | Write information to CSS console.
    * - ``void``
      - | ``writeString(java.lang.String string)``
        | Write pure string to CSS console without any extra headers in black color.
    * - ``void``
      - | ``writeString(java.lang.String string, int red, int green, int blue)``
        | Write pure string to CSS console in specified color.
    * - ``void``
      - | ``writeWarning(java.lang.String message)``
        | Write Warning information to CSS console.


Examples
--------

Some examples of display scripts are provided below, covering some common use cases:

.. toctree::

  example-action-telecommand
  example-update-widget-properties
  example-java
