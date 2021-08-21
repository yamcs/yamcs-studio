XY Graph
========

Widget for plotting one or two-dimensional data.

.. image:: _images/examples/xy-graph.png
    :alt: XY Graph
    :align: center

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

Axis Count (``axis_count``)
    The number of axis. Must be between 2 and 4. For each axis, a property group
    is added. The first axis is always considered the X axis. Up to three other axis
    may be used as Y axis.

.. include:: _props/enabled.rst
.. include:: _props/rules.rst
.. include:: _props/scripts.rst

Trace Count (``trace_count``)
    The number of traces. For each trace, a property group is added.

Trigger PV (``trigger_pv``)
    PV that serves as the trigger for traces that have the property **Update Mode**
    set to ``Trigger``.

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
.. include:: _props/forecolor_alarm_sensitive.rst
.. include:: _props/foreground_color.rst

Plot Area Background Color (``plot_area_background_color``)
    Background color used to colorize the plot area.

Show Legend (``show_legend``)
    Whether to show the trace legend.

Show Plot Area Border (``show_plot_area_border``)
    Whether to show a border around the plot area.

Show Toolbar (``show_toolbar``)
    Whether to show a toolbar with advanced controls.

Title (``title``)
    The title of the graph.

Title Font (``title_font``)
    The font used to render the title.

.. include:: _props/tooltip.rst
.. include:: _props/transparent.rst


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
.. rubric:: Axis Properties

The property names for the following properties take the form
``axis_x_property_name``, where ``x`` is the zero-based index of that axis.
The first axis is always the X axis. Others are Y axes.

Auto Scale (``axis_x_auto_scale``)
    Whether to automatically adjust the scale of this axis.

Auto Scale Treshold (``axis_x_auto_scale_treshold``)
    Value in the range ``[0-1]`` representing a portion of the plot
    area. If **Auto Scale** is enabled, it will only trigger if
    current spare space exceeds this treshold.

Axis Color (``axis_x_axis_color``)
    The color of this axis. Used for colorizing ticks, title and labels.

Axis Title (``axis_x_axis_title``)
    The title of this axis.

Dash Grid Line (``axis_x_dash_grid_line``)
    Whether to use a dashed line for the grid matching this axis. Otherwise solid.

Grid Color (``axis_x_grid_color``)
    The color of the grid matching this axis.

Log Scale (``axis_x_log_scale``)
    Use a logarithmic scale.

Maximum (``axis_x_minimum``)
    The upper bound the axis range.

Minimum (``axis_x_maximum``)
    The lower bound of the axis range.

Scale Font (``axis_x_scale_font``)
    The font used to render the scale labels.

Scale Format (``axis_x_scale_format``)
    The format used to render scale labels.

    The pattern follows Java conventions. See
    https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html

    Some examples:

    .. list-table::
        :widths: 33 33 33

        * - Value
          - Format
          - Printed
        * - 1234
          - #.00
          - 1234.00
        * - 12.3456
          - #.##
          - 12.35
        * - 1234
          - 0.###E0
          - 1.234E3

Show Grid (``axis_x_show_grid``)
    Whether to show a grid matching this axis.

Time Format (``axis_x_time_format``)
    The format used in case this axis should be used for showing time.

    .. list-table::
        :widths: 25 75

        * - Code
          - Value
        * - 0
          - None
        * - 1
          - yyyy-MM-dd HH:mm:ss
        * - 2
          - yyyy-MM-dd HH:mm:ss.SSS
        * - 3
          - HH:mm:ss
        * - 4
          - HH:mm:ss.SSS
        * - 5
          - HH:mm
        * - 6
          - yyyy-MM-dd
        * - 7
          - MMMMM d
        * - 6
          - Auto

Title Font (``axis_x_title_font``)
    Font used for rendering the axis title.

Visible (``axis_x_visible``)
    Whether this axis is visible.


..
    ---------------------------------------------------------------------------
.. rubric:: Trace Properties

The property names for the following properties take the form
``trace_x_property_name``, where ``x`` is the zero-based index of that trace.

Anti Alias (``trace_x_anti_alias``)
    Smoothen this trace.

Buffer Size (``trace_x_buffer_size``)
    Size of the FIFO buffer underlying this trace. When the buffer is full
    older items get deleted.

Concatenate Data (``trace_x_concatenate``)
    This property is only useful when using with array PVs. Leave it enabled for
    other PV types.

    If yes, whenever the array PV is updated, all of the new array entries
    are appended to the existing trace data.

    If no, whenever the array PV is updated, all of the new array entries
    replace all the existing trace data.

Line Width (``trace_x_line_width``)
    Thickness of the trace. If the **Trace Type** is set to ``Bar``, this
    signifies the bar width.

Name (``trace_x_name``)
    Name of the trace, as visible in the legend.

Plot Mode (``trace_x_plot_mode``)
    Specifies what to do when the underlying buffer fills up.

    .. list-table::
        :widths: 10 20 70

        * - Code
          - Value
          - Description
        * - 0
          - Plot last n pts.
          - Show the last updates, removing older points when buffer is full.
        * - 1
          - Plot n pts & stop.
          - Stop updating the plot when the buffer is full. No data gets removed
            from the buffer.

Point Size (``trace_x_point_size``)
    Size in pixels of points if **Point Style** is not set to ``None``.

Point Style (``trace_x_point_style``)
    How to stylize data points.

    .. list-table::
        :widths: 25 75

        * - Code
          - Value
        * - 0
          - None
        * - 1
          - Point
        * - 2
          - Circle
        * - 4
          - Filled Circle
        * - 5
          - Triangle
        * - 6
          - Filled Triangle
        * - 7
          - Square
        * - 8
          - Filled Square
        * - 9
          - Diamond
        * - 10
          - Filled Diamond
        * - 11
          - X Cross
        * - 12
          - Cross
        * - 13
          - Bar

Trace Color (``trace_x_trace_color``)
    Color of this trace.

Trace Type (``trace_x_trace_type``)
    Type of trace visualization.

    .. list-table::
        :widths: 25 75

        * - Code
          - Value
        * - 0
          - Solid Line
        * - 1
          - Dash Line
        * - 2
          - Point
        * - 4
          - Bar
        * - 5
          - Area
        * - 6
          - Line Area
        * - 7
          - Step Vertically
        * - 8
          - Step Horizontally
        * - 9
          - Dash Dot Line
        * - 10
          - Dash Dot Dot Line
        * - 11
          - Dot Line

Update Delay (``trace_x_update_delay``)
    Throttle plot updates by the given amount of milliseconds.
    In case of multiple traces, the shortest update time takes precedence.

Update Mode (``trace_x_update_mode``)
    Specify when PV updates should be added to the FIFO buffer underlying
    this trace. 
    
    .. list-table::
        :widths: 10 20 70

        * - Code
          - Value
          - Description
        * - 0
          - X or Y
          - Update the buffer whenever the X PV or the Y PV has changed. No received
            data will be missed with this mode.
        * - 1
          - X and Y
          - Update the buffer only as soon as both the X PV and the Y PV have received
            an update. Only the last value for each is added to the buffer, so it is
            possible to miss some values with this mode.
        * - 2
          - X
          - Update the buffer whenever the X PV has changed. Data coming from the
            Y PV may be missed with this mode (for example because Y PV updates
            faster than X PV).
        * - 4
          - Y
          - Update the buffer whenever the Y PV has changed. Data coming from the
            X PV may be missed with this mode (for example because X PV updates
            faster than Y PV).
        * - 5
          - Trigger
          - Update the buffer only whenever the **Trigger PV** has changed. This
            is one of the graph properties shared between all traces. Data coming
            from both the X PV and the Y PV maybe be missed with this mode (for
            example because they update faster than the trigger PV).

Visible (``trace_x_visible``)
    Whether this trace should be visible.

X Axis Index (``trace_x_x_axis_index``)
    Index of the axis that is X axis of this trace.

X PV (``trace_x_x_pv``)
    The PV providing x values. If empty, this trace is assumed to be chronological.

Y Axis Index (``trace_x_y_axis_index``)
    Index of the axis that is Y axis of this trace.

Y PV (``trace_x_y_pv``)
    The PV providing y values. By default this is set to the macro
    ``$(pv_name)`` so that a user can more simply populate only the **PV Name**
    field, as the common use case is to render plots with only one X and Y axis.


..
    ---------------------------------------------------------------------------
.. rubric:: Additional API

XY Graph widgets expose the following additional API for use in scripting:
    
``clearGraph(): void``
    Clear the graph (deletes the underlying buffer).

``getXBuffer(trace: number): double[]``
    Returns the current X axis values for the given trace.

``getYBuffer(trace: number): double[]``
    Returns the current Y axis values for the given trace.
