WidgetUtil
==========

Helper methods for creating widgets at runtime.

**createWidgetModel(** type **)**
    Returns the model for a new widget.

.. rubric:: Types

The following is a list of types for all available widgets:

.. list-table::
    :widths: 30 70

    * - Widget
      - Type
    * - Action Button
      - org.csstudio.opibuilder.widgets.ActionButton
    * - Arc
      - org.csstudio.opibuilder.widgets.arc
    * - Array
      - org.csstudio.opibuilder.widgets.array
    * - Boolean Button
      - org.csstudio.opibuilder.widgets.BoolButton
    * - Boolean Switch
      - org.csstudio.opibuilder.widgets.BoolSwitch
    * - Byte Monitor
      - org.csstudio.opibuilder.widgets.bytemonitor
    * - Check Box
      - org.csstudio.opibuilder.widgets.checkbox
    * - Choice Button
      - org.csstudio.opibuilder.widgets.ChoiceButton
    * - Combo
      - org.csstudio.opibuilder.widgets.combo
    * - Ellipse
      - org.csstudio.opibuilder.widgets.Ellipse
    * - Gauge
      - org.csstudio.opibuilder.widgets.gauge
    * - Grid Layout
      - org.csstudio.opibuilder.widgets.gridLayout
    * - Grouping Container
      - org.csstudio.opibuilder.widgets.groupingContainer
    * - Image
      - org.csstudio.opibuilder.widgets.Image
    * - Image Boolean Button
      - org.csstudio.opibuilder.widgets.ImageBoolButton
    * - Image Boolean Indicator
      - org.csstudio.opibuilder.widgets.ImageBoolIndicator
    * - Intensity Graph
      - org.csstudio.opibuilder.widgets.intensityGraph
    * - Knob
      - org.csstudio.opibuilder.widgets.knob
    * - Label
      - org.csstudio.opibuilder.widgets.Label
    * - LED
      - org.csstudio.opibuilder.widgets.LED
    * - Linking Container
      - org.csstudio.opibuilder.widgets.linkingContainer
    * - Menu Button
      - org.csstudio.opibuilder.widgets.MenuButton
    * - Meter
      - org.csstudio.opibuilder.widgets.meter
    * - Polygon
      - org.csstudio.opibuilder.widgets.polygon
    * - Polyline
      - org.csstudio.opibuilder.widgets.polyline
    * - Progress Bar
      - org.csstudio.opibuilder.widgets.progressbar
    * - Radio Box
      - org.csstudio.opibuilder.widgets.radioBox
    * - Rectangle
      - org.csstudio.opibuilder.widgets.Rectangle
    * - Rounded Rectangle
      - org.csstudio.opibuilder.widgets.RoundedRectangle
    * - Sash Container
      - org.csstudio.opibuilder.widgets.sashContainer
    * - Scaled Slider
      - org.csstudio.opibuilder.widgets.scaledslider
    * - Scrollbar
      - org.csstudio.opibuilder.widgets.scrollbar
    * - Spinner
      - org.csstudio.opibuilder.widgets.spinner
    * - Tabbed Container
      - org.csstudio.opibuilder.widgets.tab
    * - Table
      - org.csstudio.opibuilder.widgets.table
    * - Tank
      - org.csstudio.opibuilder.widgets.tank
    * - Text Input
      - org.csstudio.opibuilder.widgets.TextInput
    * - Text Update
      - org.csstudio.opibuilder.widgets.TextUpdate
    * - Thermometer
      - org.csstudio.opibuilder.widgets.thermometer
    * - Thumb Wheel
      - org.csstudio.opibuilder.widgets.ThumbWheel
    * - Web Browser
      - org.csstudio.opibuilder.widgets.webbrowser
    * - XY Graph
      - org.csstudio.opibuilder.widgets.xyGraph

.. rubric:: Example

.. code-block:: javascript

    var opiFile = triggerPV.getValue();

    var linkingContainer = WidgetUtil.createWidgetModel(
        "org.csstudio.opibuilder.widgets.linkingContainer");
    linkingContainer.setPropertyValue("opi_file", opiFile);

    // 1 = Size the container to fit the linked OPI
    linkingContainer.setPropertyValue("resize_behaviour", 1);
    // 1 = Line Style
    linkingContainer.setPropertyValue("border_style", 1);

    widget.removeAllChildren();
    widget.addChild(linkingContainer);
    widget.performAutosize();
