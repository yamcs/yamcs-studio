Widgets
=======

A display is a container for widgets.

Most widgets are backed by a PV. Some widgets (e.g. widgets used for layout) are typically not connected to a PV. Other widgets (e.g. charts) can be backed by more than one PV.


Catalogue of Widgets
--------------------

The default widgets in Yamcs Studio are listed below. Their runtime behaviour should be fairly straightforward.


Graphics
^^^^^^^^

.. list-table::
    :widths: 33 33 33

    * - |arc| Arc
      - |rectangle| Rectangle
      - |label| Label
    * - |polyline| Polyline
      - |rounded-rectangle| Rounded Rectangle
      - |image| Image
    * - |polygon| Polygon
      - |ellipse| Ellipse
      -

.. |arc| image:: _images/arc.png
.. |ellipse| image:: _images/ellipse2.png
.. |image| image:: _images/image.png
.. |label| image:: _images/label.png
.. |polygon| image:: _images/polygon.png
.. |polyline| image:: _images/polyline.png
.. |rectangle| image:: _images/rectangle2.png
.. |rounded-rectangle| image:: _images/roundedRectangle.png


Monitors
^^^^^^^^

.. list-table::
    :widths: 33 33 33

    * - |led| LED
      - |progress-bar| Progress Bar
      - |xygraph| XY Graph :sup:`*`
    * - |image-boolean-indicator| Image Boolean Indicator
      - |gauge| Gauge
      - |intensity-graph| Intensity Graph
    * - |text-update| Text Update
      - |thermometer| Thermometer
      - |byte-monitor| Byte Monitor
    * - |meter| Meter
      - |tank| Tank
      -

.. |byte-monitor| image:: _images/ByteMonitor.png
.. |gauge| image:: _images/gauge2.png
.. |image-boolean-indicator| image:: _images/imageBooleanIndicator.png
.. |intensity-graph| image:: _images/intensityGraph.png
.. |led| image:: _images/LED.png
.. |meter| image:: _images/XMeter.png
.. |progress-bar| image:: _images/ProgressBar.png
.. |tank| image:: _images/tank.png
.. |text-update| image:: _images/textUpdate.png
.. |thermometer| image:: _images/Thermo.png
.. |xygraph| image:: _images/XYGraph.png

:sup:`*` Clear the view on this widget by right-clicking on it and selecting **Clear Graph**. If you want advanced controls, like zooming, activate the toolbar by right-clicking on your widget and selecting **Show/Hide Graph Toolbar**.


Controls
^^^^^^^^

.. list-table::
    :widths: 33 33 33

    * - |action-button| Action Button :sup:`*`
      - |knob| Knob
      - |image-boolean-button| Image Boolean Button
    * - |menu-button| Menu Button
      - |scrollbar| Scrollbar
      - |check-box| Check Box
    * - |text-input| Text Input
      - |thumb-wheel| Thumb Wheel
      - |radio-box| Radio Box
    * - |spinner| Spinner
      - |boolean-switch| Boolean Switch
      - |choice-button| Choice Button
    * - |scaled-slider| Scaled Slider
      - |boolean-button| Boolean Button
      - |combo| Combo

.. |action-button| image:: _images/actionbutton.png
.. |boolean-button| image:: _images/BoolButton.png
.. |boolean-switch| image:: _images/BoolSwitch.png
.. |check-box| image:: _images/checkboxenabledon.png
.. |choice-button| image:: _images/ChoiceButton.png
.. |combo| image:: _images/combo.png
.. |image-boolean-button| image:: _images/imageButton.png
.. |knob| image:: _images/knob.png
.. |menu-button| image:: _images/menubutton.png
.. |radio-box| image:: _images/radiobutton.png
.. |scaled-slider| image:: _images/scaled_slider.png
.. |scrollbar| image:: _images/scrollbar.png
.. |spinner| image:: _images/Spinner.png
.. |text-input| image:: _images/textInput.png
.. |thumb-wheel| image:: _images/thumbwheel.png

:sup:`*` Action Buttons are often used to open other displays. Whether this opens in a new tab or in the same tab depends on how the display author constructed the display. Override the default by right-clicking the Action Button.


Others
^^^^^^

.. list-table::
    :widths: 33 33 33

    * - |table| Table
      - |grouping-container| Grouping Container
      - |sash-container| Sash Container
    * - |web-browser| Web Browser
      - |linking-container| Linking Container
      - |grid-layout| Grid Layout
    * - |array| Array
      - |tabbed-container| Tabbed Container
      -

.. |array| image:: _images/array.png
.. |grid-layout| image:: _images/grid.png
.. |grouping-container| image:: _images/groupContainer.png
.. |linking-container| image:: _images/linkingcontainer.png
.. |sash-container| image:: _images/SashContainer.png
.. |tabbed-container| image:: _images/tab.png
.. |table| image:: _images/table.png
.. |web-browser| image:: _images/web_browser.png


Color Decorations
-----------------

When a widget is backed by a PV, it will be decorated according to its runtime state. The specific colors of these decorations can vary since the default colors can be overridden (or disabled) by the display author.

Connected
    No decorations

Connected, but no value (yet)
    Dashed pink border around the widget

Disconnected
    Solid pink border around the widget and the label 'Disconnected' in the top left corner (space-permitting)

Expired
    Blinking solid pink border around the widget

Minor Alarm
    Solid orange border around the widget

Major Alarm
    Solid red border around the widget


Note that the color information for alarms is currently not as rich as it could be. Yamcs parameters support five different levels of alarms, as well as a range of special monitoring values. This information is transformed using the following mapping:

* WATCH, WARNING, DISTRESS → MINOR
* CRITICAL, SEVERE → MAJOR
