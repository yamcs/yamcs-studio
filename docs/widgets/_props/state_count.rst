State Count (``state_count``)
    If set to more than two, this boolean widget becomes a multistate widget, and a number of properties
    will be added to control each state's specific options: **Color**, **Label** and **Value**.

    The ``Bit`` property then has no more meaning. Instead the widget state will be determined by
    comparing the widget's numeric value with each state's **Value** property.

    Multistate works with numerical values only. It does not work when the
    **Data Type** is set to ``Enum``.
