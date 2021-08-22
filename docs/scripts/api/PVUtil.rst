PVUtil
======

Helper methods for working with PV objects.

**createPV(** pvName, widget **)**
    Create, start and return a :doc:`PV` attached to the given widget.

    If the widget is destroyed (for example, because the display
    is closed), the PV goes down with it automatically.

    The pvName argument can be any name that can also be used
    as a PV Name property. For example: ``loc://foo``,
    ``/myproject/Voltage``, ``=2 + 3``, ``sim://noise``, ...

**getDouble(** pv **)**
    Return a double representation for the current value of
    the given PV.

**getDoubleArray(** pv **)**
    Return an array of doubles for the current value of the
    provided PV.

    The array length is 1 if the value is not an array.

**getLong(** pv **)**
    Return a long representation for the current value of the
    given PV.

**getLongArray(** pv **)**
    Return an array of longs for the current value of the
    provided PV.

    The array length is 1 if the value is not an array.

**getString(** pv **)**
    Return a string representation for the current value of
    the given PV.

**getStringArray(** pv **)**
    If pv is an array PV, return its current value as an array
    of strings, where each entry is a string representation of
    the actual value.

    The array length is 1 if the value is not an array.

**getTimeInMilliseconds(** pv **)**
    Returns the time for the PV's current value as the number
    of milliseconds since the UNIX epoch. If the PV is backed
    by a Yamcs parameter, this would match that parameter's
    generation time.

**getTimeString(** pv [, pattern] **)**
    Return a formatted string for the timestamp of the PV's current
    value. If the PV is backed by a Yamcs parameter, this would
    match that parameter's generation time.

    If the **pattern** argument is provided, it must be a string
    that follows Java conventions. See
    https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html

**getSeverity(** pv **)**
    Returns the current severity of the given PV as a number with
    the following mapping:

    * ``-1``: UNDEFINED or INVALID
    * ``0``: NONE
    * ``1``: MAJOR
    * ``2``: MINOR

**getSeverityString(** pv **)**
    Returns the string value of the current severity of the given
    PV. One of ``UNDEFINED``, ``INVALID``, ``NONE``, ``MINOR`` or
    ``MAJOR``.

**getLabels(** pv **)**
    Returns an array with all possible enum values for an enumerated PV.

**getSize(** pv **)**
    Return the array length of the current PV value. If the
    current value is not an array, returns ``1``.

**writePV(** pvName, value [, timeout] **)**
    Write a value to a specific PV Name. In the background
    this will create a temporary PV object, write to it,
    and finally stop that PV.

    A custom timeout in seconds may be provided. The default
    is 10 seconds.

    This method has no return value. It returns immediately
    and does not wait for the write (or timeout) to occur.

    .. note::

        If you already have a connected :doc:`PV` instance ``x``,
        it is simpler to call ``x.setValue(value)`` on it.


.. rubric:: Example
    
Create a PV, listen to it, then write to it.

.. code-block:: javascript

    var IPVListener = Java.type("org.yamcs.studio.data.IPVListener");

    var myPV = PVUtil.createPV("loc://test", widget);
    myPV.addListener(new IPVListener({
        valueChanged: function(pv) {
            ConsoleUtil.writeInfo("Write successful: " + PVUtil.getString(pv));
        }
    }));

    ConsoleUtil.writeInfo("Writing...");
    myPV.setValue(123);

Set a widget property based on a PV's severity.

.. code-block:: javascript

    var severity = PVUtil.getSeverityString(pvs[0]);
    var color;
    switch (severity) {
        case "NONE":
            color = ColorFontUtil.GREEN;
            break;
        case "MAJOR":
            color = ColorFontUtil.RED;
            break;
        case "MINOR":
            color = ColorFontUtil.ORANGE;
            break;
        default:
            color = ColorFontUtil.PINK;
    }
    widget.setPropertyValue("foreground_color", color);
