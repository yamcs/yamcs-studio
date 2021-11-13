Array
=====

Widget for reading or writing an array of other widgets of the same type.

.. image:: _images/examples/array.png
    :alt: Array
    :align: center

When dropping a new widget onto an array widget, that widget will be
duplicated by the length of that array. Each singular contained widget
is matched to one element of the array.

While editing in Yamcs Studio, changes to any widget element are applied
to all other widgets within that array.

If the array widget is backed by an array PV, the **Array Length** as
well as **Data Type** are automatically determined from the PV.

An array widget can support large array sizes by showing a scrollbar,
and a spinner that allows to jump to a specific array index.


..
    ---------------------------------------------------------------------------
.. rubric:: Basic Properties

.. include:: _props/name.rst
.. include:: _props/pv_name.rst
.. include:: _props/widget_type.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Behavior Properties

.. include:: _props/actions.rst

Array Length (``array_length``)
    Number of array elements.

    This property is not available when the array is backed by a PV.

Data Type (``data_type``)
    Type of the array. This is the type of the value returned by
    ``widget.getValue()`` inside a script.

    This property is not available when the array is backed by a PV.

    .. tabularcolumns:: \Yc{0.25}\Y{0.75}

    .. list-table::
        :header-rows: 1
        :widths: 25 75

        * - Code
          - Value
        * - 0
          - double[]
        * - 1
          - String[]
        * - 2
          - int[]
        * - 3
          - byte[]
        * - 4
          - long[]
        * - 5
          - short[]
        * - 6
          - float[]
        * - 7
          - Object[]

Enabled (``enabled``)
    Unset to make contained control widgets unusable.

.. include:: _props/rules.rst
.. include:: _props/scripts.rst
.. include:: _props/visible.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Border Properties

.. include:: _props/border_alarm_sensitive.rst
.. include:: _props/border_color.rst
.. include:: _props/border_style.rst
.. include:: _props/border_width.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Display Properties

.. include:: _props/alarm_pulsing.rst
.. include:: _props/backcolor_alarm_sensitive.rst
.. include:: _props/background_color.rst
.. include:: _props/font.rst
.. include:: _props/forecolor_alarm_sensitive.rst
.. include:: _props/foreground_color.rst

Horizontal (``horizontal``)
    If yes, the array elements are arranged in horizontal direction. Otherwise vertical.

Show Scrollbar (``show_scrollbar``)
    Make a scrollbar visible.

Show Spinner (``show_spinner``)
    Make a spinner widget for jumping to a specific index within the array.

Spinner Width (``spinner_width``)
    The width in pixels of the spinner widget.

.. include:: _props/tooltip.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Additional API

Array widgets expose the following additional :doc:`../scripts/api/Widget`
API for use in scripting:
    
**getIndex(** child **)**
    Get the index of a child widget. If the given widget is not a child
    this method returns ``-1``.
