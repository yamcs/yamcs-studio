Accessing Java
==============

In addition to the common :doc:`api/index` utilities, Yamcs Studio allows
bridged access within scripts to entire Java packages.

Yamcs Studio uses the Eclipse RCP framework, so for example, here we make
use of JFace to open a message dialog:

.. code-block:: javascript

    var MessageDialog = Java.type("org.eclipse.jface.dialogs.MessageDialog");
    MessageDialog.openInformation(
        null, "Attention", "I was triggered by a script");

If you go in this direction, at some point it may make more sense to
extend or fork Yamcs Studio at a Java level, rather than using display
scripting.

Note further that such scripts are not compatible when rendering your
OPI displays via the Yamcs web interface.

Full documentation of Java and Eclipse APIs is well outside of scope of
this document, so we leave this topic with a complex example that uses
Eclipse SWT and Java I/O. The script logic is as follows:

#. Download a random image from Internet.
#. Save this image to a file within the workspace.
#. Create or reuse a standalone window to display the image.

.. literalinclude:: _includes/java-example.js
    :language: javascript
