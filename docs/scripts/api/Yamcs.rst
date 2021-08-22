Yamcs
=====

The following methods are available.

**issueCommand(** commandName, args **)**
    Issue a telecommand on the currently connected
    Yamcs processor.


.. rubric:: Example

.. code-block:: javascript

    Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON', {
        voltage_num: 1
    });
