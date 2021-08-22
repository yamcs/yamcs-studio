Scripts
=======

Widgets have many properties and methods that can be modified dynamically. :doc:`../rules/index`
are one way to do that. For more advanced manipulations and control logic, scripts are the right tool.

Scripts can be attached to any widget (or the display itself) and execute only when one
of their declared *trigger PVs* gets updated.

Yamcs Studio supports scripts in two different languages:

JavaScript
    Supports ECMAScript 5.1, and a limited set of ECMAScript 6 features.

    The Java implementation that executes JavaScript is called Nashorn.
    For more on Nashorn, refer to https://github.com/openjdk/nashorn.

Python
    Supports Python 2.7.

    The Java implementation that executes Python scripts is called Jython.
    For more on Jython, refer to https://www.jython.org.

    Note that Yamcs Studio does *not* make use of the Python distribution
    available to your system.


Within both scripting languages you have bridged access to the same Java API
that allows interacting with its widget, other widgets, PVs or Yamcs Studio
itself. Only the syntax looks different.

JavaScript is generally preferred, because it increases the interoperability
of your OPI files with the web implementation available on the Yamcs web
interface. Python is not at all supported by Yamcs web.

.. rubric:: Isolation

Each script runs with separate scope. It is therefore not possible
to access variables from within another script. If you want to share
data between scripts, it is possible to do so by creating a local PV
and using that as a means for communication.

On the other hand, consecutive executions *do* make use of the same
scope, so it is possible to memorize state by using global variables.


.. toctree::
    :maxdepth: 1

    attach
    api/index
    java
