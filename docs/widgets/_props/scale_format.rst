Scale Format (``scale_format``)
    Pattern describing how to format step values shown on the scale.

    The pattern follows Java conventions. See
    https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html

    Some examples:

    .. list-table::
        :header-rows: 1
        :widths: 33 33 33

        * - Value
          - Format
          - Printed
        * - 1234
          - #.00
          - 1234.00
        * - 12.3456
          - #.##
          - 12.35
        * - 1234
          - 0.###E0
          - 1.234E3
