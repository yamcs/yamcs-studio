GUIUtil
=======

Helper methods for window-level operations on Yamcs Studio.

**fullScreen()**
    Enter or exit full-screen.

**openConfirmDialog(** message **)**
    Open a confirmation dialog with the given message.

    Returns ``true`` if the user confirmed.

**openConfirmDialogAsync(** message, onResult **)**
    Open a confirmation dialog with the given message, without
    blocking the script.

    ``onResult`` is a function that is called with ``true`` if
    the user confirmed, ``false`` otherwise.

    In the Yamcs web interface, this shows an application
    dialog, whereas ``openConfirmDialog`` relies on a
    browser dialog. Prefer this method for displays that are
    used in both.

**openInformationDialog(** message **)**
    Open an information dialog with the given message.

**openWarningDialog(** message **)**
    Open a warning dialog with the given message.

**openErrorDialog(** message **)**
    Open an error dialog with the given message.

**openPasswordDialog(** message, password **)**
    Open a password input dialog.

    Returns ``true`` if the user entered the expected password.

    .. note::

        OPIs are rendered on the client. The use of this method
        provides only a false sense of security.

**openPasswordDialogAsync(** message, password, onResult **)**
    Open a password input dialog, without blocking the script.

    ``onResult`` is a function that is called with ``true`` if
    the user entered the expected password, ``false`` otherwise.

    In the Yamcs web interface, this shows an application
    dialog with a masked input field, whereas
    ``openPasswordDialog`` relies on a browser dialog.

    The note of ``openPasswordDialog`` applies here too.


.. rubric:: Examples

.. code-block:: javascript

    if (GUIUtil.openConfirmDialog("Are you sure?")) {
        // Do something clever.
    }

.. code-block:: javascript

    GUIUtil.openConfirmDialogAsync("Are you sure?", function (confirmed) {
        if (confirmed) {
            // Do something clever.
        }
    });
