Parameters
==========

Parameter PVs represent a value that is provided by a connected Yamcs server, usually from packet telemetry.

The PV Name for parameters is the fully qualified name as identified in the Yamcs Mission Database.

Example PV Names:

* ``/YSS/SIMULATOR/BatteryVoltage1``
* ``/YSS/SIMULATOR/BatteryTemperature1``

In these examples ``YSS`` is the name of the root space system. ``SIMULATOR`` is the name of the space system directly below, which defines both measurements ``BatteryVoltage1`` and ``BatteryTemperature1``.


.. rubric:: LOLO/LO/HI/HIHI Markers

Several widgets support the display of low/high warning and alarm markers. If those
widgets have the property **Limits from PV** enabled, the marker positions are mapped
from Yamcs severity levels:

* WATCH, WARNING, DISTRESS → LO/HI
* CRITICAL, SEVERE → LOLO/HIHI


.. rubric:: Minimum/Maximum

Several widgets display the current PV value on a scale. If those widgets have
the property **Limits from PV** enabled:

* The lower bound of the scale matches the most-severe lower bound of the parameter.
  If Yamcs does not specify any lower bound, the widget reverts to the configured
  **Minimum** property.
 
* The upper bound of the scale matches the most-severe upper bound of the parameter.
  If Yamcs does not specify any upper bound, the widget reverts to the configured
  **Maximum** property.
 

  .. rubric:: Units

  Some widgets have the property **Units from PV**. If enabled, Yamcs Studio will
  compose a unit string based on information provided by Yamcs.
