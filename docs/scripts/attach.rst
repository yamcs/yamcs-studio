Attach a Script
===============

Scripts can be attached to any widget, or the display itself.

Select the **Scripts** property in the **Properties** view, to open the
**Attach Scripts** dialog.


.. image:: _images/attach-scripts.png
    :alt: Attach a Script
    :align: center

Use this dialog to add or remove scripts. A script can point to a
Script file available in the workspace, or alternatively it may be
embedded directly within the display.


.. rubric:: Input PVs

The panel to the right lists the PVs that must be accessible during
script execution. They are available from the global array ``pvs`` in
the same order as they appear in the dialog.

Each script must have at least one Input PV that has the
**Trigger** checkbox enabled, otherwise it would never execute.

If you require to execute a script that only runs when the display
initializes, use a formula ``=1`` as your trigger PV.

By default a script executes only when all of its inputs are connected and have a value,
and one of its trigger PVs is updated.


.. rubric:: Options

The default conditions for execution can be customized in the **Options**
tab:

Execute anyway even if some PVs are disconnected.
    Set this flag to run a script even if one of the input PVs is not yet available.

Do not execute the script if error was detected.
    Set this flag to abort further executions of this script as soon
    as a failure is detected.
