Script API
==========

.. rubric:: Global Variables

The following global variables are available in all scripts:

``triggerPV``
    :doc:`PV` object for the PV that triggered this particular script
    execution. Sample usage:

    .. code-block:: javascript

        if (triggerPV === pvs[1]) {
            ConsoleUtil.writeInfo("I was triggered by the second input PV.");
        }

``pvs``
    Array of :doc:`PV` objects. One for each Input PV. The order in the
    array matches with the order in which they appear in the
    :doc:`Attach Scripts <../attach>` dialog.

``widget``
    :doc:`Widget` object for the widget that this script is attached to.

``display``
    :doc:`Widget` object for the display that this script's widget belongs to.


.. rubric:: Utilities
.. toctree::
    :maxdepth: 1

    ColorFontUtil
    ConsoleUtil
    DataUtil
    FileUtil
    GUIUtil
    ParameterInfo
    PVUtil
    ScriptUtil
    WidgetUtil
    Yamcs

.. rubric:: Support Classes
.. toctree::
    :maxdepth: 1

    PV
    Widget
