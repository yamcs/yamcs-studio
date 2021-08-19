Rounded Rectangle
=================

Widget that draws a rectangle shape with rounded corners.

.. opi:: ../capture/widgets/rounded_rectangle/rounded_rectangle.opi.png

Example of shape fill:

.. opi:: ../capture/widgets/rounded_rectangle/fill.opi.png

    * - Foreground Color
      - :color:`rgb(255, 0, 0)`
    * - Fill Level
      - 40.0
    * - Horizontal Fill
      - no
    * - Transparent
      - yes
    * - Line Color
      - :color:`rgb(255, 0, 0)`
    * - Line Width
      - 1


Example of gradient effect:

.. opi:: ../capture/widgets/rounded_rectangle/gradient.opi.png

    * - Background Color
      - :color:`rgb(191, 191, 191)`
    * - Fill Level
      - 40.0
    * - Foreground Color
      - :color:`rgb(114, 250, 120)`
    * - Horizontal Fill
      - no
    * - Gradient
      - yes
    * - Line Color
      - :color:`rgb(161, 161, 161)`
    * - Line Width
      - 1


The shape background and foreground colors can be made alarm-aware by attaching a PV.
Note that the PV value is otherwise ignored. In particular: it does not impact fill level (use
a :doc:`tank` for this use case).

.. opi:: ../capture/widgets/rounded_rectangle/alarm.opi.png

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
    The color of the rectangle shape.

.. include:: _props/bg_gradient_color.rst
.. include:: _props/corner_height.rst
.. include:: _props/corner_width.rst
.. include:: _props/fill_level.rst
.. include:: _props/font.rst
.. include:: _props/forecolor_alarm_sensitive.rst

Foreground Color (``foreground_color``)
    The color of the shape fill.

.. include:: _props/fg_gradient_color.rst
.. include:: _props/gradient.rst
.. include:: _props/horizontal_fill.rst
.. include:: _props/line_color.rst
.. include:: _props/line_style.rst
.. include:: _props/line_width.rst
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
