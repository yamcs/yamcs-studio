Menu Button
===========

Control widget for opening a menu when clicked. A menu item is created for each
action.


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

Actions from PV (``actions_from_pv``)
    If the PV is an enumerated PV, populate actions based on the list of
    enumeration states. When selected, each action will write that
    specific state value to the attached PV.

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
.. include:: _props/backcolor_alarm_sensitive.rst
.. include:: _props/background_color.rst
.. include:: _props/font.rst
.. include:: _props/forecolor_alarm_sensitive.rst
.. include:: _props/foreground_color.rst
.. include:: _props/label.rst

Show Down Arrow (``show_down_arrow``)
    Show a down chevron next to the label.

.. include:: _props/tooltip.rst

Transparent (``transparent``)
    Do not draw the button background.


..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst
