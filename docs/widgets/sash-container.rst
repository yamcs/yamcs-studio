Sash Container
==============

Container widget for grouping widgets in two panels. X and Y coordinates of contained
widgets are relative to the top-left of the specific sash panel they belong to.

The panels can be oriented left/right or top/bottom. The available container
space can be redistributed at runtime by dragging the sash.

.. image:: _images/examples/sash-container.png
    :alt: Sash Container
    :align: center

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

Horizontal (``horizontal``)
    If yes, the sash uses a left and a right panel. Otherwise the sash
    uses a top panel and a bottom panel.

Sash Position (``sash_position``)
    Initial position of the sash (value between 0 and 1).

Sash Style (``sash_style``)
    Select the style of the sash:

    .. tabularcolumns:: \Yc{0.25}\Y{0.75}

    .. list-table::
        :header-rows: 1
        :widths: 25 75

        * - Code
          - Value
        * - 0
          - None
        * - 1
          - Rounded
        * - 2
          - Ridged
        * - 3
          - Etched
        * - 4
          - Line
        * - 5
          - Double Lines

Sash Width (``sash_width``)
    Width in pixels of the sash. A minimum width of 1 is enforced.

.. include:: _props/tooltip.rst

Transparent (``transparent``)
    Make the container background transparent.


..
    ---------------------------------------------------------------------------
.. rubric:: Panel 1 (Left/Up) Properties

Auto Scale Children (``panel1_auto_scale_children``)
    If yes, the contained widgets are auto scaled while the sash
    divider is being moved. This auto scaling respects the **Scale Options**
    set by each widget.

    If no, contained widgets keep their size, and a scrollbar is
    added to the sash panel when necessary.


..
    ---------------------------------------------------------------------------
.. rubric:: Panel 2 (Right/Down) Properties

Auto Scale Children (``panel2_auto_scale_children``)
    If yes, the contained widgets are auto scaled while the sash
    divider is being moved. This auto scaling respects the **Scale Options**
    set by each widget.

    If no, contained widgets keep their size, and a scrollbar is
    added to the sash panel when necessary.

..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst
