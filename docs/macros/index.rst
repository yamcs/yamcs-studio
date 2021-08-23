Macros
======

Macros are placeholders that can be used in string-based property values.
They are substituted at runtime, and before the property gets
interpreted.

Macros use the syntax ``${mymacro}`` or ``$(mymacro)``.


.. rubric:: Predefined Macros

The following macros are predefined with special meaning:

``$(DID)``
    Unique identifier for a display instance.

    For example, if a display uses a :doc:`local PV </pv/loc>` called
    ``loc://$(DID)_foo``, each instance of that display will use a
    different local variable, without interfering with potential other
    instances of that display.

``$(DNAME)``
    The name of the display.

``$(LCID)``
    Unique identifier for a :doc:`/widgets/linking-container` instance.
    If a display contains multiple Linking Container instances referring
    to the same display, the ``$(LCID)`` macro will evaluate to a different
    unique value in each.


.. rubric:: Property Macros

In the context of a widget, all of its properties can be accessed
in other properties using property identifiers as the macro name.
For example: ``$(pv_name)``, ``$(border_width)``, ...


.. rubric:: Custom Macros

You can define custom macros at different locations:

#. In user preferences under ``OPI Runtime``.
#. In the configuration of an **Open OPI** widget action
   (or the equivalent **ScriptUtil.openOPI** script utility).
#. In the **Macros** property of a display.
#. In the **Macros** property of a container widget.

Macros are evaluated for each widget. If a macro with the same name
is defined at multiple levels, they overwrite each other in the
above order. For example: (4) wins from (1).

If the option **Include macros from parent** is unticked, macros
from higher levels are not inherited.


.. rubric:: Example

The **Tooltip** property of all widgets has the following default value:

.. code-block:: text

    $(pv_name)
    $(pv_value)

When the display is running, the tooltip of widgets will show the value
of the **PV Name** property, as well as its current value.
