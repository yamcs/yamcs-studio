Display
=======

Root container widget for an OPI file.


..
    ---------------------------------------------------------------------------
.. rubric:: Basic Properties

Macros (``macros``)
    Manage the macros available within this container.

.. include:: _props/name.rst
.. include:: _props/widget_type.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Behavior Properties

.. include:: _props/actions.rst

Auto Scale Widgets (``auto_scale_widgets``)
    Controls whether autoscaling is enabled.

    If so, child widgets are automatically stretched in horizontal
    and/or vertical direction depending on their individual
    **Scale Options** property configuration.

    Autoscaling can cause shifts in the interdistance between
    widgets.

Auto Zoom to Fit All (``auto_zoom_to_fit_all``)
    If enabled, always zoom the display to fit the available
    space while preserving ratios.

.. include:: _props/rules.rst
.. include:: _props/scripts.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Display Properties

.. include:: _props/background_color.rst

Grid Color (``foreground_color``)
    Color of grid lines.

Grid Space (``grid_space``)
    Space in pixels between grid lines.

Show Close Button (``show_close_button``)
    If true, this display's tab does not show the close icon at
    runtime.

    Note that the tab can still be closed through right-click.

Show Edit Range (``show_edit_range``)
    If true, two lines matching the display's width and height
    are visible while editing this OPI display.

Show Grid (``show_grid``)
    If true, a grid is visible while editing this OPI display.

Show Ruler (``show_ruler``)
    If true, vertical and horizontal rulers are visible while
    editing this OPI Display.

Snap to Geometry (``snap_to_geometry``)
    If true, dragging widgets while editing an OPI display makes
    the positioning *sticky* with respect to the geometry of other
    widgets.

..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst
