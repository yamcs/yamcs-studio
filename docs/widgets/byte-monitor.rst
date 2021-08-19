Byte Monitor
============

Widget that displays the bits of a numeric value as a series of LEDs.


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

.. include:: _props/effect_3d.rst
.. include:: _props/alarm_pulsing.rst
.. include:: _props/backcolor_alarm_sensitive.rst
.. include:: _props/background_color.rst
.. include:: _props/font.rst
.. include:: _props/forecolor_alarm_sensitive.rst
.. include:: _props/foreground_color.rst

Horizontal (``horizontal``)
    Direction of the LEDs.

Labels (``label``)
    The labels corresponding with each LED. Labels are only
    visible when **Square LED** is enabled.

LED Border (``led_border``)
    The width of the border surrounding each LED bulb.

LED Border Color (``led_border_color``)
    The color of the border surrounding each LED bulb.

Number of Bits (``num_bits``)
  The number of bits (LEDs) to display.

Off Color (``off_color``)
    Color of each LED when it is off.

On Color (``on_color``)
    Color of each LED when it is on.

Pack LEDs (``led_packed``)
    Collapse borders between successive LEDs. Has best
    effect when using square LEDs.

Reverse Bits (``bitReverse``)
    Left (or top) bit corresponds with the least-significant bit.

.. include:: _props/square_led.rst

Start Bit (``startBit``)
    Bit position to start displaying from (zero-based).

.. include:: _props/tooltip.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst
