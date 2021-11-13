Linking Container
=================

Container widget that allows to include another OPI file, or a
specific group of widgets within another OPI file.

A typical use case would be a common header or footer shown on
a series of displays. Or to reuse the same OPI with different
*variables*, that is: using macros.

The bounds of a Linking Container can be automatically calculated by right-clicking
it and choosing **Perform Auto Size**. The new size will account for all content
to be visible.


..
    ---------------------------------------------------------------------------
.. rubric:: Basic Properties

.. include:: _props/macros.rst
.. include:: _props/name.rst
.. include:: _props/widget_type.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Behavior Properties

.. include:: _props/actions.rst

Enabled (``enabled``)
    Unset to make contained control widgets unusable.

Group Name (``group_name``)
    The name of a specific Grouping Container inside the
    linked display.

    If set, only this particular group will be visible.
    Otherwise the entire linked display is visible.

OPI File (``opi_file``)
    The linked OPI display file.

.. include:: _props/rules.rst
.. include:: _props/scripts.rst
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

Resize Behaviour (``resize_behaviour``)
    How to match the size of the Linking Container with that of the
    linked display or group.

    .. tabularcolumns:: \Yc{0.25}\Y{0.75}

    .. list-table::
        :header-rows: 1
        :widths: 25 75

        * - Code
          - Value
        * - 0
          - Size \*.opi to fit the container
        * - 1
          - Size the container to fit the linked \*.opi
        * - 2
          - Don't resize anything, crop if \*.opi too large for container
        * - 3
          - Don't resize anything, add scrollbars if \*.opi too large for container


.. include:: _props/tooltip.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst
