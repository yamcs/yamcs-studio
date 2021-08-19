Check Box
=========

Widget for writing 0 or 1 to the attached PV. This widget can also be used to flip a single bit of the attached PV.


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

Bit (``bit``)
    Matches the widget's boolean value to a specific bit of the attached PV's value.

    If ``-1``, any non-zero value is considered true, whereas a zero value
    is considered false.

    This widget writes the numeric values 0 (unselected) or 1 (selected) to the PV.

.. include:: _props/enabled.rst
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
.. include:: _props/auto_size2.rst
.. include:: _props/backcolor_alarm_sensitive.rst
.. include:: _props/background_color.rst
.. include:: _props/font.rst
.. include:: _props/forecolor_alarm_sensitive.rst
.. include:: _props/foreground_color.rst
.. include:: _props/label.rst

Selected Color (``selected_color``)
    The color of the check mark.

.. include:: _props/tooltip.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst
