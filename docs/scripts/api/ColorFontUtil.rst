ColorFontUtil
=============

Utility class for creating font and color objects, required
to interact with some widget properties.

The following colors are directly variable:

``BLACK``,
``BLUE``,
``CYAN``,
``DARK_GRAY``,
``GRAY``,
``GREEN``,
``LIGHT_BLUE``,
``ORANGE``,
``PINK``,
``PURPLE``,
``RED``,
``WHITE``,
``YELLOW``.

Other colors can be created using these methods:

**getColorFromRGB(** red, green, blue **)**
    Returns a color object for the specified red, green and blue
    components.

**getColorFromHSB(** hue, saturation, brightness **)**
    Returns a color object based on an HSB representation.

    * **hue**: value between 0 and 360
    * **saturation**: value between 0 and 1
    * **brightness**: value between 0 and 1

To create a font object use this method:

**getFont(** name, height, style **)**
    Returns a font object.

    * **name**: the name of the font
    * **height**: font height in points
    * **style**: One of:

      * ``0`` for normal
      * ``1`` for bold
      * ``2`` for italic
      * ``3`` for bold and italic


.. rubric:: Example

The following scripts do the same but in different ways:

.. code-block:: javascript

    var red = ColorFontUtil.RED;
    widget.setPropertyValue("background_color", red);

.. code-block:: javascript

    var red = ColorFontUtil.getColorFromRGB(255, 0, 0);
    widget.setPropertyValue("background_color", red);

.. code-block:: javascript

    var red = ColorFontUtil.getColorFromHSB(0, 1, 1);
    widget.setPropertyValue("background_color", red);
