GUIUtil
=======

Helper methods for window-level operations on Yamcs Studio.

**fullScreen()**
    Enter or exit full-screen.

**openConfirmDialog(** message **)**
    Open a confirmation dialog with the given message.

    Returns ``true`` if the user confirmed.

**openPasswordDialog(** message, password **)**
    Open a password input dialog.

    Returns ``true`` if the user entered the expected password.

    .. note::

        OPIs are rendered on the client. The use of this method
        provides only a false sense of security.


.. rubric:: Example

.. code-block:: javascript

    if (GUIUtil.openConfirmDialog("Are you sure?")) {
        // Do something clever.
    }
