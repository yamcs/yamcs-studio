LED
===

Boolean widget that displays a value as an ON/OFF LED.

.. opi:: ../capture/widgets/led/led.opi.png

The LED can be made square.

.. opi:: ../capture/widgets/led/square.opi.png

    * - Square LED
      - yes
    * - Show Boolean Label
      - yes
    * - Width
      - 40
    * - Height
      - 30


This widget further supports multistate whereby it can assume multiple different color states.

.. opi:: ../capture/widgets/led/multistate.opi.png

    * - State Count
      - 3
    * - 3D Effect
      - no


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
.. include:: _props/bit1.rst
.. include:: _props/data_type.rst
.. include:: _props/off_state.rst
.. include:: _props/on_state.rst
.. include:: _props/rules.rst
.. include:: _props/scripts.rst
.. include:: _props/state_count.rst
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
.. include:: _props/bulb_border.rst
.. include:: _props/bulb_border_color.rst
.. include:: _props/font.rst
.. include:: _props/forecolor_alarm_sensitive.rst
.. include:: _props/foreground_color.rst

Off Color (``off_color``)
    Color of the LED when it is off.

.. include:: _props/off_label.rst

On Color (``on_color``)
    Color of the LED when it is on.

.. include:: _props/on_label.rst
.. include:: _props/show_boolean_label.rst
.. include:: _props/square_led.rst
.. include:: _props/tooltip.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst
