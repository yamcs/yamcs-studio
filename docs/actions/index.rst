Actions
=======

Widgets have an **Actions** property which is used to trigger actions upon user interaction. The common case is to trigger Actions with an :doc:`../widgets/action-button`.

.. image:: _images/open-opi-action.png
    :alt: Example Action
    :align: center

Actions can be linked to a push event of a button, or the release event of a toggle
button. All actions of a widget are also available via the right-click context menu
of that widget.

When using a widget scripts, an action can also be triggered programmatically using
:doc:`executeAction(index) </scripts/api/Widget>`.

The available actions are:

Open OPI
    | Opens another OPI.
    | Indicate the workspace path to the OPI with ``File Path``.
    | Use the ``Mode`` to select whether the OPI should by default open in the same tab. Note that the runtime user can override this default behaviour by right-clicking the button.

Write PV
    Writes the specified value to a PV. The :doc:`macro </macros/index>` ``$(pv_name)`` is automatically substituted with the PV attached to the widget.

Execute System Command
    This executes a command on your operating system.

Execute JavaScript
    Execute a JavaScript. Link to a script file in your workspace, or alternatively embed it into the Action.

Execute Python Script
    Execute some Python script. Link to a script file in your workspace, or alternatively embed it into the Action.

Run Command
    This runs a telecommand on the currently connected Yamcs processor.

Run Command Stack
    This runs all commands in a Yamcs Command Stack file (extension ``*.ycs``).

Play WAV File
    Plays the specified sound file.

Open File
    Opens a workspace file with the default handler.

Open Webpage
    Open the specified web page with the integrated web browser.
