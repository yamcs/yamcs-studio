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
    Returns the *monitoring result* of a Yamcs parameter. One of ``IN_LIMITS``, ``DISABLED``, ``WATCH``, ``WARNING``, ``DISTRESS``, ``CRITICAL`` or ``SEVERE``.

    For PVs that are not connected to Yamcs parameters, this will always return null.

.. rubric:: Example

.. code-block:: javascript

    Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON', {
        voltage_num: 1
    });

.. code-block:: javascript

    Yamcs.runCommandStack('/My Project/stacks/example.ycs');
