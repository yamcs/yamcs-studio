Borders
=======

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


Yamcs parameters support five different levels of alarms, as well as a range of special monitoring values. This information is transformed using the following mapping:

* WATCH, WARNING, DISTRESS → MINOR
* CRITICAL, SEVERE → MAJOR
