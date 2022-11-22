Connect to Yamcs
================

Yamcs Studio can connect as a client to Yamcs Server.

Yamcs Server, or 'Yamcs', handles the processing, archiving and dispatching of telemetry data. Yamcs Studio is one of the possible Yamcs clients for receiving telemetry data.

To configure a Yamcs connection, select **Yamcs > Connect...**. This will open the Connections window where you can manage your connections.

.. image:: _images/connections.png
    :alt: Connections
    :align: center

Click |server_add| **Add Server** to add a server connection, or |server_remove| **Remove Server** to remove the selected server connection.

The right panel contains editable details for the selected server connection:

Server URL (required)
    Specify the base URL for reaching Yamcs. For example ``http://localhost:8090``

Instance (required)
    Yamcs can run multiple instances in parallel. You can think of instances like different environments, where every instance is completely separated from the other instance. While Yamcs Server may be running multiple instances in parallel, Yamcs Studio will always connects the user to one specific instance, which you have to configure here.

Type (required)
    Specify the authentication mechanism. One of ``Standard`` or ``Kerberos``.
    
    If your Yamcs installation does not require authentication ``Standard`` and do not enter a username.


.. rubric:: Standard Authentication

This authentication type applies the standard authentication mechanism of Yamcs where the initial authentication is done by sending a username and password to Yamcs.

User / Password (optional)
    If your Yamcs instance is secured, fill in your user and password here.

    Optionally, the password can be stored to your OS keychain. If you don't do so, you
    will be prompted each time you attempt to connect to Yamcs.

    If you wish to remove a previously stored password, click **Clear**.


.. rubric:: Kerberos Authentication

Using Kerberos, Yamcs Studio assumes the username of the OS user. There is no need for a password, because the protocol allows Yamcs to verify authentication against your Kerberos Distribution Center (KDC).

The use of this mechanism requires extra configuration of your Yamcs Server, to integrate it with your Kerberos realm.


.. note::

    Connection preferences are stored at user level (not workspace), and will continue functioning whenever you upgrade your copy of Yamcs Studio.

    The storage mechanism and location is different for each platform:

    Linux
       XML file at ``~/.java/.userPrefs/org/yamcs/studio/ui/connections/prefs.xml``
    MacOS
       Property list file at ``~/Library/Preferences/org.yamcs.studio.plist``
    Windows
       In the Windows Registry: ``HKEY_CURRENT_USER\Software\JavaSoft\Prefs\org\yamcs\studio\ui\connections``

    Any saved passwords are stored separately from the connections using `Eclipse Secure Storage <https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Freference%2Fref-securestorage-start.htm>`_.

.. |server_add| image:: _images/server_add.png
.. |server_remove| image:: _images/server_remove.png
