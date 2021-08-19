Arc
===

Widget that draws an arc shape.

.. image:: ../capture/widgets/arc/arc.opi.png
    :align: center

Example of shape fill:

.. opi:: ../capture/widgets/arc/fill.opi.png

      * - Background Color
        - rgb(51, 255, 0)
      * - Fill
        - yes
      * - Foreground Color
        - rgb(0, 0, 255)
      * - Line Style
        - Dot
      * - Line Width
        - 2
      * - Start Angle
        - 30
      * - Total Angle
        - 120

The shape background and foreground colors can be made alarm-aware by attaching a PV.
Note that the PV value is otherwise ignored.

.. opi:: ../capture/widgets/arc/alarm.opi.png

    * - BackColor Alarm Sensitive
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
    The color of the wedge when **Fill** is enabled.

.. include:: _props/fill.rst
.. include:: _props/font.rst
.. include:: _props/forecolor_alarm_sensitive.rst

Foreground Color (``foreground_color``)
    The color of the arc stroke.

.. include:: _props/line_style.rst
.. include:: _props/line_width.rst
.. include:: _props/start_angle.rst
.. include:: _props/tooltip.rst
.. include:: _props/total_angle.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst
