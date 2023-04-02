Yamcs
=====

The following methods are available.

**issueCommand(** commandName, args **)**
    Issue a telecommand on the currently connected
    Yamcs processor.

**getMonitoringResult(** pv **)**
    Returns the *monitoring result* of a Yamcs parameter. One of ``IN_LIMITS``, ``DISABLED``, ``WATCH``, ``WARNING``, ``DISTRESS``, ``CRITICAL`` or ``SEVERE``.

    For PVs that are not connected to Yamcs parameters, this will always return null.

.. rubric:: Example

.. code-block:: javascript

    Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON', {
        voltage_num: 1
    });
