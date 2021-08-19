Grid Layout
===========

Meta widget that can be added to a container for laying out its other
contained widgets in a grid.

While editing in Yamcs Studio, a grid icon is visible in the top-left
of the container. Right-click this icon and choose **Layout Widgets**
to calculate and apply adjusted positions for all widgets belonging
to that container.

When running a display, the layout is always calculated prior to
rendering.

Grid cell sizes are established using the following algorithm:

#. Group the widgets in rows, respecting the order visible in the Outline view.
#. Per row: apply the largest widget width to all widgets (based on bounding box).
#. Per column: apply the largest widget height (based on bounding box).

.. note::

    Some widgets perform autosizing which may cause the widget area to not use
    the entire available cell space.


..
    ---------------------------------------------------------------------------
.. rubric:: Basic Properties

.. include:: _props/name.rst
.. include:: _props/widget_type.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Display Properties

Fill Grids (``fill_grids``)
    Resize smaller widgets to match the largest widget, thereby
    filling up the cells.

Grid Gap (``grid_gap``)
    Reserve a gap in pixels between grid cells.

Number of Columns (``number_of_columns``)
    Number of columns in the grid.
