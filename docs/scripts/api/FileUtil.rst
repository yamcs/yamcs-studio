FileUtil
========

Helper methods for working with files.

Absolute workspace paths take the form /MyProject/some/file
where MyProject is the name of one of the projects in your
workspace.

When providing a relative workspace path, you must pass the
current widget too (available in all scripts using the
global variable ``widget``).

**readTextFile(** path [, widget] **)**
    Returns the content of a file as a string.

    The path can be an absolute path on the local file
    system, or a relative path inside the workspace. In case
    of a relative path, the widget argument must be provided
    as the reference for resolving the path.

**readTextFileAsync(** path, widget, onSuccess [, onError] **)**
    Reads the content of a file as a string, without blocking
    the script.

    ``onSuccess`` is a function that is called with the content
    of the file. ``onError`` is an optional function that is
    called with an error message if the file cannot be read.
    Without ``onError``, errors are written to the console.

    The path is interpreted as for ``readTextFile``. The
    widget argument may be ``null`` if the path is absolute.

    Unlike ``readTextFile``, this method is also supported by
    the Yamcs web interface, where it reads files from the same
    bucket as the display. Prefer this method for displays that
    are used in both.

**writeTextFile(** path, inWorkspace [, widget], text, append **)**
    Writes a string to a file.

    The path can be an absolute path on the local file
    system, or a relative path inside the workspace. In case
    of a relative path, the widget argument must be provided
    as the reference for resolving the path.

    ``inWorkspace`` must be true if the path should be
    interpreted as a workspace path. False for a local file
    system file. This allows to distinguish a workspace path
    like /MyProject/somefile.txt from an equally valid local
    file system path.

    Set ``append`` to true, if the file must be appended to
    if it already exists.

**getInputStreamFromFile(** path [, widget] **)**
    Return a ``java.io.InputStream`` for a file.

    The path can be an absolute path on the local file
    system, or a relative path inside the workspace. In case
    of a relative path, the widget argument must be provided
    as the reference for resolving the path.

    This is an advanced method which provides access to
    a pure Java object. Use with caution, and close
    the stream when you're done.

**loadXMLFile(** path [, widget] **)**
    Return an ``org.jdom2.Element`` for an XML file.

    The path can be an absolute path on the local file
    system, or a relative path inside the workspace. In case
    of a relative path, the widget argument must be provided
    as the reference for resolving the path.

    This is an advanced method which provides access to
    a pure Java object. Use with caution.

**openFile(** path [, widget] **)**
    Open a file with the default editor.

    The path can be an absolute path on the local file
    system, or a relative path inside the workspace. In case
    of a relative path, the widget argument must be provided
    as the reference for resolving the path.

**openWebPage(** url **)**
    Open the given URL with a web browser.

**playWavFile(** path [, widget] **)**
    Play a WAV file.

    The path can be an absolute path on the local file
    system, or a relative path inside the workspace. In case
    of a relative path, the widget argument must be provided
    as the reference for resolving the path.

**openFileDialog(** inWorkspace **)**
    Open a file selector.

    If ``inWorkspace`` is true, the selector will only allow
    workspace files to be chosen. If false, the selector
    allows to choose from the local file system.

    This method returns the selected path, or null
    if the user cancelled.

**openFileDialog(** startingFolder **)**
    Open a local file system selector starting at the
    specified folder.

    This method returns the selected path, or null
    if the user cancelled.

**saveFileDialog(** inWorkspace **)**
    Open a save-file selector.

    If ``inWorkspace`` is true, the selector will only allow
    workspace files to be chosen. If false, the selector
    allows to choose from the local file system.

    This method returns the selected save path, or null
    if the user cancelled.

**saveFileDialog(** startingFolder **)**
    Open a local file system save-file selector.

    This method returns the selected save path, or null
    if the user cancelled.

**openDirectoryDialog()**
    Open a directory selector on the local file system.

    This method returns the selected path, or null
    if the user cancelled.

**openDirectoryDialog(** startingFolder **)**
    Open a directory selector on the local file system
    starting at the specified folder.

    This method returns the selected path, or null
    if the user cancelled.

**workspacePathToSysPath(** workspacePath **)**
    Returns the local file system path for the given workspace path.


.. rubric:: Examples

.. code-block:: javascript

    FileUtil.readTextFileAsync("data.txt", widget, function (text) {
        display.getWidget("myLabel").setPropertyValue("text", text);
    }, function (error) {
        ConsoleUtil.writeError(error);
    });

.. code-block:: javascript

    var targetFile = FileUtil.saveFileDialog(false);
    if (targetFile) {
        var text = "file content";
        FileUtil.writeTextFile(targetFile, false, text, false);
    }
