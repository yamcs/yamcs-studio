Parameters
==========

Parameter PVs represent a read-only value that is provided by a connected Yamcs server, usually from packet telemetry.

The PV Name for parameters is the fully qualified name as identified in the Yamcs Mission Database.

Example PV Names:

* ``/YSS/SIMULATOR/BatteryVoltage1``
* ``/YSS/SIMULATOR/BatteryTemperature1``

In these examples ``YSS`` is the name of the root space system. ``SIMULATOR`` is the name of the space system directly below, which defines both measurements ``BatteryVoltage1`` and ``BatteryTemperature1``.
