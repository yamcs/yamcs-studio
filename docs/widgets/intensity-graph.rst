Intensity Graph
===============

Widget that displays an array of numbers as an image. An Intensity Graph
could be used to display a video image, temperature pattern or terrain.

.. image:: _images/examples/intensity-graph.png
    :alt: Intensity Graph
    :align: center

An Intensity Graph can be used in two different modes toggled by the
property **RGB Mode**:

#. When not in RGB mode, this widget uses a configurable color map
   that associates a color with each possible array entry value.

   The input data must then be provided as a single dimensioned array
   where rows are concatenated to each other. For example, consider
   the following one-dimensional input data:

   [p\ :sub:`1,1`, p\ :sub:`1,2`, ..., p\ :sub:`1,y`
   , p\ :sub:`2,1`, p\ :sub:`2,2`, ..., p\ :sub:`2,y`
   , ..., p\ :sub:`x,1`, p\ :sub:`x,2`, ..., p\ :sub:`x,y`]

   The widget will interpret this data as a two-dimensional array with
   ``x`` rows and ``y`` columns:

   | [ p\ :sub:`1,1`, p\ :sub:`1,2`, ..., p\ :sub:`1,y` ]
   | [ p\ :sub:`2,1`, p\ :sub:`2,2`, ..., p\ :sub:`2,y` ]
   | ...
   | [ p\ :sub:`x,1`, p\ :sub:`x,2`, ..., p\ :sub:`x,y` ]

#. When in RGB Mode, the input data is again a one-dimensional
   array of values, but the number of elements (**Data Height**
   times **Data Width**) must be multiplied by three for describing
   the red, green and blue values of each pixel. The structure of
   the data is similar as when using a color map, but each pixel
   is described by three values: red, green and blue.

   [p\ :sub:`1,1,r`, p\ :sub:`1,1,g`, p\ :sub:`1,1,b`
   , ..., p\ :sub:`x,y,r`, p\ :sub:`x,y,g`, p\ :sub:`x,y,b`]


This widget also allows connecting PVs that will receive updates with
the profile data matching the input.


..
    ---------------------------------------------------------------------------
.. rubric:: Basic Properties

Horizon Profile X PV (``horizon_profile_x_pv_name``)
    The output PV which will receive updates with the horizontal profile data
    on the X axis.

Horizon Profile Y PV (``horizon_profile_y_pv_name``)
    The output PV which will receive updates with the horizontal profile data
    on the Y axis.

.. include:: _props/name.rst

Pixel Info PV (``pixel_info_pv_name``)
    The output PV which will receive updates when the user hovers the graph,
    or clicks on it. Each such event is sent as as a ``VTable`` to the PV.

    The ``VTable`` has the following columns:

    * ``X``: X coordinate of the pixel.
    * ``Y``: Y coordinate of the pixel.
    * ``Value``: Value of the image data for this specific pixel.
    * ``Selected``: True if the user was pressing the mouse button. False otherwise.

.. include:: _props/pv_name.rst

Vertical Profile X PV (``vertical_profile_x_pv_name``)
    The output PV which will receives updates with the vertical profile data
    on the X axis.

Vertical Profile Y PV (``vertical_profile_y_pv_name``)
    The output PV which will receives updates with the vertical profile data
    on the Y axis.

.. include:: _props/widget_type.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Behavior Properties

.. include:: _props/actions.rst
.. include:: _props/crop_bottom.rst
.. include:: _props/crop_left.rst
.. include:: _props/crop_right.rst
.. include:: _props/crop_top.rst

Data Height (``data_height``)
    Number of rows of the input data.

Data Width (``data_width``)
    Number of rows of the input data.

.. include:: _props/enabled.rst

Maximum (``maximum``)
    The upper limit for a singular array element.

Minimum (``minimum``)
    The lower limit for a singular array element.

Profile on Single Line (``single_line_profiling``)
    If set, profile on a single pixel. A crosshair is added
    to the graph which can be adjusted by dragging it around.

    If unset, the profiling is performed on the average of
    all pixels.

RGB Mode (``rgb_mode``)
    Enable RGB mode. In this mode, the color map is
    not available, and RGB values for each pixel must
    be provided.

    Profiles take the average of r, g and b for each point.

ROI Count (``roi_count``)
    The number of regions of interest.

    Regions of interest are marked on the graph. For each
    region of interest a specific set of properties is
    added as detailed in **ROI Properties**.

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

Color Map (``color_map``)
    Color map for the graph. This maps values to colors. In the dialog
    you can choose from a set of predefined color maps (defaulting to JET)
    or make one yourself.

    If the option **Interpolate** is selected, the color mapping will be done
    through linear interpolation of the colors available in the map.

    If the option **Auto Scale** is selected, the value range of the color map
    is remapped to the range indicated by the properties **Minimum** and
    **Maximum**. If unselected, there is no remapping, and the lookup is direct.

.. include:: _props/font.rst
.. include:: _props/forecolor_alarm_sensitive.rst
.. include:: _props/foreground_color.rst

ROI Color (``roi_color``)
    The color of the frame for indicating a region of interest.

Show Ramp (``show_ramp``)
    Whether to show the color map legend to the right of the graph.

.. include:: _props/tooltip.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

Graph Area Height (``graph_area_height``)
    The height of the actual graph area. The widget size will adjust
    accordingly.

Graph Area Width (``graph_area_width``)
    The width of the actual graph area. The widget size will adjust
    accordingly.

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst


..
    ---------------------------------------------------------------------------
.. rubric:: ROI Properties

The property names for the following properties take the form
``roi_x_property_name``, where ``x`` is the zero-based index of that ROI.

Height PV (``roi_x_height_pv``)
    PV that provides the height of the ROI.

Title (``roi_x_title``)
    The label for this ROI, as shown on the graph.

Visible (``roi_x_visible``)
    Whether this ROI is visible on the graph.

Width PV (``roi_x_width_pv``)
    PV that provides the width of the ROI.

X PV (``roi_x_x_pv``)
    PV that provides the X coordinate of the ROI.

Y PV (``roi_x_y_pv``)
    PV that provides the Y coordinate of the ROI.


..
    ---------------------------------------------------------------------------
.. rubric:: X Axis Properties

Axis Color (``x_axis_axis_color``)
    Color used for rendering X axis ticks and labels.

Axis Title (``x_axis_axis_title``)
    Label for the X axis.

Major Tick Step Hint (``x_axis_major_tick_step_hint``)
    The minimum space in pixels between major ticks.

Maximum (``x_axis_maximum``)
    Upper range of the axis.

Minimum (``x_axis_minimum``)
    Lower range of the axis.

Scale Font (``x_axis_scale_font``)
    Font used for axis labels.

Show Minor Ticks (``x_axis_show_minor_ticks``)
    Whether to show minor ticks on the scale.

Title Font (``x_axis_title_font``)
    Font used for the axis title.

Visible (``x_axis_visible``)
    Whether to show the X axis.


..
    ---------------------------------------------------------------------------
.. rubric:: Y Axis Properties

Axis Color (``y_axis_axis_color``)
    Color used for rendering Y axis ticks and labels.

Axis Title (``y_axis_axis_title``)
    Label for the Y axis.

Major Tick Step Hint (``y_axis_major_tick_step_hint``)
    The minimum space in pixels between major ticks.

Maximum (``y_axis_maximum``)
    Upper range of the axis.

Minimum (``y_axis_minimum``)
    Lower range of the axis.

Scale Font (``y_axis_scale_font``)
    Font used for axis labels.

Show Minor Ticks (``y_axis_show_minor_ticks``)
    Whether to show minor ticks on the scale.

Title Font (``y_axis_title_font``)
    Font used for the axis title.

Visible (``y_axis_visible``)
    Whether to show the Y axis.
