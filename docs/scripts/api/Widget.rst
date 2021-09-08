Widget
======

When a script is executed it has access to a ``widget`` variable which
allows for interaction with the widget that the script is attached to.

Other widgets can also be retrieved using the ``display`` variable:

    .. code-block:: javascript
        
        otherWidget = display.getWidget("someOtherWidgetName");


.. rubric:: All Widgets

The following methods are common to all widgets:

**executeAction(** index **)**
    Run one of the widget's actions by specifying its
    index (zero-based).

**getPropertyValue(** name **)**
    Retrieve the value for one of the widget's properties.

**setPropertyValue(** name, value [, force] **)**
    Update a widget property to a new value.

    If force is true, the update occurs even if the new value
    equals the current value.

**getPVByName(** pvName **)**
    Retrieve one of the widget's PVs by name. This includes PVs
    used for both rules and scripts.

    Returns a :doc:`PV` object, or null if no PV with this name
    is known to the widget.

**setVar(** name, value **)**
    Save any custom data for later retrieval by name.

**getVar(** name **)**
    Retrieve custom data by name.

**getMacroValue(** name **)**
    Retrieve a macro string value by name.

**getName()**
    Returns the widget's name.

    Shortcut for ``getPropertyValue("name")``.

**setX(** x **)**
    Update the X coordinate of the widget area.

    Shortcut for ``setPropertyValue("x", x)``.

**setY(** y **)**
    Update the Y coordinate of the widget area.

    Shortcut for ``setPropertyValue("y", y)``.

**setWidth(** width **)**
    Update the width of the widget area.

    Shortcut for ``setPropertyValue("width", width)``.

**setHeight(** height **)**
    Update the height of the widget area.

    Shortcut for ``setPropertyValue("height", height)``.

**setEnabled(** enabled **)**
    Control whether the widget is enabled.

**setVisible(** visible **)**
    Update the visiblity of this widget.

    Shortcut for ``setPropertyValue("visible", visible)``.

**getValue()**
    Return the value of the *widget*.
    
    A widget can have a value without having an attached PV.
    If it does have an attached PV, the value of the widget is
    almost always identical to that of the PV.

**setValue(** value **)**
    Manually set the value of the *widget*.

    This does *not* update any PV. It updates only the displayed
    value. If the widget is also following updates from a PV, the
    value may well be overwritten whenever that PV updates.

    This method should be called on the UI thread. If unsure,
    use the otherwise identical method ``setValueInUIThread``.

**setValueInUIThread(** value **)**
    Same as ``setValue``, but forces a switch to the UI thread.


.. rubric:: PV Widgets

Widgets that have the **PV Name** property can read or write a PV.
They have the following additional methods:

**getPV(** [propertyName] **)**
    Return the :doc:`PV` object for a specific widget property.

    If ``propertyName`` is not specified, it defaults to ``pv_name``,
    which is the name of the main PV property **PV Name**.

**getPVName()**
    Returns a string with the main **PV Name** for this widget.

**getAllPVNames()**
    Return a string array with all PV Names that are used in
    any of the widget's PV-like properties.

**getPVValue(** propertyName **)**
    Return the current value for a specific PV property.

**setPVValue(** propertyName, value **)**
    Write a new value to the PV used by the given property.


.. rubric:: Container Widgets

Container widgets are those that contain other widgets:

* :doc:`../../widgets/array`
* :doc:`../../widgets/display`
* :doc:`../../widgets/grouping-container`
* :doc:`../../widgets/linking-container`
* :doc:`../../widgets/sash-container`
* :doc:`../../widgets/tabbed-container`

They have the following additional methods:

**getWidget(** name  **)**
    Get a descendant widget of this container by name.

**getChild(** name **)**
    Get a direct child widget of this container by name.

**getChildren()**
    Returns all direct child widgets, in order.

**addChild(** widgetModel **)**
    Add a child widget to this container.

    ``widgetModel`` is an object that can be obtained
    using :doc:`WidgetUtil.createWidgetModel <WidgetUtil>`

**addChildToRight(** widgetModel **)**
    Add a child widget to this container, while adjusting
    its X coordinate such that it is added to the right
    of other child widgets.

    ``widgetModel`` is an object that can be obtained
    using :doc:`WidgetUtil.createWidgetModel <WidgetUtil>`

**addChildToBottom(** widgetModel **)**
    Add a child widget to this container, while adjusting
    its Y coordinate such that it is added below other
    child widgets.

    ``widgetModel`` is an object that can be obtained
    using :doc:`WidgetUtil.createWidgetModel <WidgetUtil>`

**removeChild(** widget **)**
    Remove the given child widget from this container.

**removeChildByName(** name **)**
    Remove the direct child widget of this container by name.

**removeAllChildren()**
    Remove all child widgets.

**performAutosize()**
    Adjust the container size to fit its child widgets.

**getValue()**
    Returns an array with all of its children's values
    (unless ``setValue`` was used to set another type
    of value).

**setValue(** value **)**
    If the given value is an array of length equal to the
    number of child widgets, the values are written
    respectively to those widgets.

    Otherwise, the value as a whole is written to each child.


.. rubric:: Examples

Update a widget's value *without* using the **PV Name**
property:

.. code-block:: javascript

    var v = PVUtil.getString(pvs[0]);
    if (v == "DISABLED") {
        widget.setValue(1.0);
    } else if (v == "OK") {
        widget.setValue(2.0);
    } else {
        widget.setValue(3.0);
    }

    widget.setPropertyValue("tooltip", v);


Print the name and type of a container's children:

.. code-block:: javascript

    var children = widget.getChildren();
    for (var i = 0; i < children.length; i++) {
        var child = children[i];
        var name = child.getPropertyValue("name");
        var type = child.getPropertyValue("widget_type");
        ConsoleUtil.writeInfo("Widget: " + name + ", type: " + type);
    }
