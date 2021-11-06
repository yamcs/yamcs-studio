Limits from PV (``limits_from_pv``)
    Determine Minimum, LOLO, LO, HI, HIHI, Maximum levels automatically based on the underlying PV.

    If the PV is backed by a Yamcs parameter, the mapping is as follows:

    .. list-table::
        :header-rows: 1
        :widths: 50 50
        
        * - Yamcs
          - Yamcs Studio
        * - WATCH
          - LO/HI
        * - WARNING
          - LO/HI
        * - DISTRESS
          - LO/HI
        * - CRITICAL
          - LOLO/HIHI
        * - SEVERE
          - LOLO/HIHI
