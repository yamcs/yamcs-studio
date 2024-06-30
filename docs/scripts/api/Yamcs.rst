Yamcs
=====

The following methods are available.

**issueCommand(** commandName, args **)**
    Issue a telecommand on the currently connected
    Yamcs processor.

**runCommandStack(** path [, widget] **)**
    Runs all commands in a Yamcs Command Stack (``*.ycs``).

    The path can be an absolute path on the local file
    system, or a relative path inside the workspace. In case
    of a relative path, the widget argument must be provided
    as the reference for resolving the path.

**getMonitoringResult(** pv **)**
    Returns the *monitoring result* of a Yamcs Parameter PV. One of ``IN_LIMITS``, ``WATCH``, ``WARNING``, ``DISTRESS``, ``CRITICAL`` or ``SEVERE``.

    Returns null when no monitoring check was performed.

**getAcquisitionStatus(** pv **)**
    Returns the *acquisition status* of a Yamcs Parameter PV. One of ``ACQUIRED``, ``NOT_RECEIVED``, ``INVALID`` or ``EXPIRED``.

    .. versionadded:: 1.7.5

**getGenerationTime(** pv **)**
    Returns the *generation time* of a Yamcs Parameter PV in ISO-8601 format.

    .. versionadded:: 1.7.5

**getReceptionTime(** pv **)**
    Returns the *reception time* of a Yamcs Parameter PV in ISO-8601 format.

    .. versionadded:: 1.7.5

**getParameterInfo(** pv **)**
    Returns a :doc:`ParameterInfo` object containing the MDB info for a Yamcs Parameter PV.

    .. versionadded:: 1.7.5


.. rubric:: Example

.. code-block:: javascript

    Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON', {
        voltage_num: 1
    });

.. code-block:: javascript

    Yamcs.runCommandStack('/My Project/stacks/example.ycs');
