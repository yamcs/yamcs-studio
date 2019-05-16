Installation
============


Prerequisites
-------------

Java 8 or higher must be installed.

64-bit packages are generated for Linux, Windows and Mac OS.


Install Manually
----------------

`Download <https://yamcs.org/downloads/>`_ the latest Yamcs Studio release for your platform. Extract to your preferred location and launch it. When it asks you to choose a workspace, choose a new directory where you have write rights , e.g. under your home directory. Workspaces contain displays, scripts and user preferences. By default your workspace will be populated with a few sample projects. These projects contain displays that show simulated parameters as produced by a default-configured Yamcs Server.


Install from Repository
-----------------------

On Linux distributions you can install the open-source version of Yamcs Studio via a package repository. Configure the Yamcs repository appropriate to your distribution following the `repository instructions <https://yamcs.org/downloads/>`_.


RPM (RHEL, Fedora, CentOS)
^^^^^^^^^^^^^^^^^^^^^^^^^^

Install via ``dnf`` (or ``yum`` on older distributions)

.. code:: shell

    $ dnf check-update
    $ sudo dnf install yamcs-studio


RPM (SLE, openSUSE)
^^^^^^^^^^^^^^^^^^^

.. code:: shell

    $ sudo zypper refresh
    $ sudo zypper install yamcs-studio


..
    APT (Debian, Ubuntu)
    ^^^^^^^^^^^^^^^^^^^^

    .. code:: shell

        $ sudo apt-get update
        $ sudo apt-get install yamcs-studio


Troubleshooting
---------------

Most problems related to starting Yamcs Studio have to do with Java not being correctly detected, or by trying to launch Yamcs Studio with an old version of Java. Both of these issues are usually resolved by installing a recent Java JDK.

In case that didn't help, Try defining the ``-vm`` property in the root ``Yamcs Studio.ini`` file. Refer to the instructions available at `<https://wiki.eclipse.org/Eclipse.ini>`_.
