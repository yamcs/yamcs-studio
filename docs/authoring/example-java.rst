Example Java
============

A simple example showing a dialog:

.. code:: javascript

    importPackage(Packages.org.eclipse.jface.dialogs);
    MessageDialog.openInformation(
        null, "Dialog from JavaScript", "This is a dialog opened from JavaScript")


A more complex example using Java SWT shell, Java IO and Files. When the script is triggered, it retreives a random image on the Internet, save it to the disk in the workspace, creates a Java window and displays the image. If the script is triggered multiple times, it retrieves the previously used shell and udpates its content:

.. literalinclude:: java-example.js
    :language: javascript
