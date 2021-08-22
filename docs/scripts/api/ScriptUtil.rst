ScriptUtil
==========

The following methods are available.

**openOPI(** widget, path, target [, macros] **)**
    Open an OPI specified by an absolute or relative
    workspace path.

    Relative paths are resolved in relation to the
    display file of the provided widget object.

    ``target`` can take the following values:

    * ``0``: New tab
    * ``1``: Replace current OPI.
    * ``2``: New window
    * ``7``: Detached view
    * ``8``: New shell

    Custom ``macros`` can be provided to the new OPI.
    Use :doc:`DataUtil.createMacrosInput() <DataUtil>`

**closeCurrentOPI()**
    Close the currently active OPI display.

**closeAssociatedOPI(** widget **)**
    Close the OPI display that hosts the given widget.

**executeSystemCommand(** command, timeout **)**
    Run a local system command.

    The timeout argument indicates the maximum number of seconds
    that the command is allowed to execute.

    This method returns immediately. Any stdout or stderr
    is sent to the **Console** view.

**execInUI(** runnable, widget **)**
    Run some logic on the UI thread.

    ``runnable`` must be a ``java.lang.Runnable`` implementation.

    This is an advanced method. Use with caution.

**executeEclipseCommand(** commandId [, parameters] **)**
    Execute an eclipse command by specifying its identifier,
    and optionally a String array with parameters.

    This is an advanced method. Available command IDs are not
    documented, and need to be reverse engineered from source
    code.


.. rubric:: Example

.. code-block:: javascript

    ConsoleUtil.writeError("Something went wrong");
