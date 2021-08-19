Scrollbar
=========

Widget for writing to a numeric PV.


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

Bar Length (``bar_length``)
    Thumb size, relative to the value range.

.. include:: _props/enabled.rst
.. include:: _props/limits_from_pv2.rst
.. include:: _props/maximum.rst
.. include:: _props/minimum.rst

Page Increment (``page_increment``)
    Increment added/subtracted when:

    * Clicking the scroll track next to the thumb.
    * Pressing :kbd:`PgUp` or :kbd:`PgDn`.

.. include:: _props/rules.rst
.. include:: _props/scripts.rst

Step Increment (``step_increment``)
    Increment added/subtracted when:
    
    * Clicking the arrow buttons.
    * Pressing :kbd:`Left` or :kbd:`Right` (in case of a horizontal bar).
    * Pressing :kbd:`Up` or :kbd:`Down` (in case of a vertical bar).

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
    Yes for a horizontal bar, otherwise vertical.

Show Value Tip (``show_value_tip``)
    Display the current value while it is changing.

.. include:: _props/tooltip.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst
