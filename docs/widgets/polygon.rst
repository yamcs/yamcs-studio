Polygon
=======

Widget that draws a polygon shape.

.. opi:: ../capture/widgets/polygon/polygon.opi.png

To draw a polygon, choose the tool from the Palette, and click on the
start location. Every next click will add a new point. Double-click to
indicate this is the last point. The last point is connected to the
first point to form a closed shape. Points can be repositioned using the
yellow handles.

Example of shape fill:

.. opi:: ../capture/widgets/polygon/fill.opi.png

    * - Fill Level
      - 20.0
    * - Horizontal Fill
      - yes
    * - Transparent
      - yes
    * - Line Width
      - 1


The shape background and foreground colors can be made alarm-aware by attaching a PV.
Note that the PV value is otherwise ignored.

.. opi:: ../capture/widgets/polygon/alarm.opi.png

    * - ForeColor Alarm Sensitive
      - yes


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
.. include:: _props/rules.rst
.. include:: _props/scripts.rst
.. include:: _props/visible.rst
..
    .. include:: _props/enabled.rst -- TODO? Why visible in Yamcs Studio?


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
.. include:: _props/alpha.rst
.. include:: _props/anti_alias.rst
.. include:: _props/backcolor_alarm_sensitive.rst

Background Color (``background_color``)
    The color of the polygon shape.

.. include:: _props/fill_level.rst
.. include:: _props/font.rst
.. include:: _props/forecolor_alarm_sensitive.rst

Foreground Color (``foreground_color``)
    The color of the shape fill.

.. include:: _props/horizontal_fill.rst
.. include:: _props/line_color.rst
.. include:: _props/line_style.rst
.. include:: _props/line_width.rst
.. include:: _props/points.rst
.. include:: _props/rotation_angle.rst
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
