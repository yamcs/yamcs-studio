Format Type (``format_type``)
    How to format the PV value.

    .. tabularcolumns:: \Yc{0.1}\Yl{0.2}\Y{0.7}

    .. list-table::
        :header-rows: 1
        :widths: 10 20 70
        
        * - Code
          - Value
          - Description
        * - 0
          - Default
          - Use a default format type according to the value type.
        * - 1
          - Decimal
          - 
        * - 2
          - Exponential
          - Example: ``2.023E10``
        * - 3
          - Hex 32
          - Example: ``0xFDC205``
        * - 4
          - String
          - Print a string representation
        * - 5
          - Hex 64
          - Same as "Hex 32", but supporting long numbers too. Example: ``0xF0DEADBEEF``
        * - 6
          - Compact
          - If the value is numeric, use either a Decimal or Exponential format, whichever is the shortest.
        * - 7
          - Engineering
          - Use engineering notation: exponent of ten is power of a thousand. Example: ``20.23E9``
        * - 8
          - Sexagesimal
          - Format as degrees (or hours), minutes, and seconds with colons inbetween. Example: ``12:45:10.2``
        * - 9
          - Sexagesimal HMS
          - Same as sexagesimal, but the value is assumed to be in radians, and expressed as hours, minutes and seconds.
        * - 10
          - Sexagesimal DMS
          - Same as sexagesimal, but the value is assumed to be in radians, and expressed as degrees, minutes and seconds.
        * - 11
          - Time String (Unix Millis)
          - Print Unix Milliseconds as a time string. The format is specified in **Preferences > Date Format** (default: ``yyyy-MM-dd HH:mm:ss.SSS``).
