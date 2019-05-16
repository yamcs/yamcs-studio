Capturing Log Output
====================

In case you need to debug an issue with a deployed Yamcs Studio client, it can be useful to capture the logging output. Instructions are specific to the platform.


.. rubric:: Linux

Launch the executable from a terminal window while redirecting all output to a file named ``log.txt``

.. code::

    ./Yamcs\ Studio >log.txt 2>&1


.. rubric:: Mac OS X

With Terminal navigate into the Yamcs Studio application bundle and launch the executable directly from there while redirecting all output to a file named ``log.txt``. For example:

.. code::

    cd Yamcs\ Studio.app/Contents/MacOS
    ./Yamcs\ Studio >log.txt 2>&1


.. rubric:: Windows

With Command Prompt navigate into the location where you installed Yamcs Studio and launch the executable while redirecting all output to a file named ``log.txt``. For example:

.. code::

    "Yamcs Studio.exe" >log.txt 2>&1
