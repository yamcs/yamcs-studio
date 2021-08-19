Grouping Container
==================

Container widget for grouping other widgets. When moving a grouping container, all
of its contained widgets are moved with it. X and Y coordinates of contained widgets
are relative to the top-left of Grouping Container.

By default a Grouping Container is *Unlocked* as indicated in the top-left corner.
Click this indicator to switch the container to *Locked*. Once locked you will not
be able to directly select its contained widgets.

Widgets can be added to a container in two ways:

* By dragging and dropping widgets onto the area of an existing Grouping Container.
* By selecting existing widgets and choosing **Create Group** from the right-click
  context menu.

The bounds of a Grouping Container can be automatically calculated by right-clicking
it and choosing **Perform Auto Size**. The new size will account for all the contained
widgets to be visible.

Deleting a Grouping Container will also delete all of its children. You can also
delete the group without deleting the children, by right-clicking it and choosing
**Remove Group**.


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

Enabled (``enabled``)
    Unset to make contained control widgets unusable.

Forward Colors (``fc``)
    If yes, the **Background Color** and **Foreground Color** set by this
    container are applied to all contained widgets.

Lock Children (``lock_children``)
    If yes, contained widgets are not directly selectable.

.. include:: _props/rules.rst
.. include:: _props/scripts.rst

Show Scrollbar (``show_scrollbar``)
    Show a scrollbar when necessary.

.. include:: _props/visible.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Border Properties

.. include:: _props/border_color.rst
.. include:: _props/border_style.rst
.. include:: _props/border_width.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Display Properties

.. include:: _props/background_color.rst
.. include:: _props/font.rst
.. include:: _props/foreground_color.rst
.. include:: _props/tooltip.rst

Transparent Background (``transparent``)
    Make the container background transparent.


..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst
