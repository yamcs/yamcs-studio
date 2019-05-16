Connecting to Yamcs
===================

Yamcs Studio is a client application that is meant to be connected with Yamcs Server.

Yamcs Server, or 'Yamcs', handles the processing, archiving and dispatching of telemetry data. Yamcs Studio is one of the possible Yamcs clients for receiving telemetry data.

To configure a Yamcs connection, select **File > Connect...**. This will open the Connections window where you can manage your connections. For many missions, one connection will do just fine, but depending on how Yamcs is deployed at your site, you may have multiple Yamcs instances on the same server, or even multiple Yamcs servers.

.. image:: _images/connections.png
    :alt: Connections
    :align: center

Click |server_add| **Add Server** to add a server connection, or |server_remove| **Remove Server** to remove the selected server connection.


Connection Properties
---------------------

The right panel contains editable details for the selected server connection. We document the available properties below, but if you're unsure what to fill in, ask details to the person that is responsible for installing Yamcs at your site.

Yamcs Instance (required)
    Yamcs can run multiple instances in parallel. You can think of instances like different environments, where every instance is completely separated from the other instance. While Yamcs Server may be running multiple instances in parallel, Yamcs Studio will always connects the user to one specific instance, which you have to configure here.

User / Password (optional)
    If your Yamcs instance is secured, fill in your user and password here.

Server (required)
    Specify your actual host and port connection details here. The port is usually 8090.

Name (required)
    You can give your configuration a name of your choosing. This name will be used to represent this connection in the left panel of the Connections window.

Save Password (optional)
    If you prefer not to enter your password at every occasion, tick this box to save your password to disk. Please be aware that your password will be saved in a manner that is difficult, but not impossible, for an intruder to break.


Connecting
----------

All changes you make are automatically saved when you click **Connect**. If you want to discard your changes click **Cancel**.

Select the **Connect on startup** option, if you would like Yamcs Studio to automatically reconnect to the last used Yamcs instance during start-up. If this connection requires privileges and you chose not to save your password to disk, you will see a specialised login window everytime you start Yamcs Studio:

.. image:: _images/login.png
    :alt: Login
    :align: center

.. note::

    Connection preferences are stored in a hidden folder under your home directory, and will continue functioning whenever you upgrade your copy of Yamcs Studio.


You can verify that your copy of Yamcs Studio is properly connected by looking at the bottom status bar:

.. image:: _images/processor.png
    :alt: Realtime Processor
    :align: center

If it says ``realtime``, then you've successfully connected.


.. |server_add| image:: _images/server_add.png
.. |server_remove| image:: _images/server_remove.png
