Data Type (``data_type``)
    Control how the widget boolean value is established.

    .. list-table::
        :header-rows: 1
        :widths: 10 20 70
        
        * - Code
          - Value
          - Description
        * - 0
          - Bit
          - The widget boolean value matches a specific bit (indicated by the **Bit** property), or the entire value in case the **Bit** property is set to ``-1``
        * - 1
          - Enum
          - The widget boolean value follows the comparison of its value with specific enumeration states (indicated with the **Off State** and **On State** property
