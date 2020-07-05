Processed Variables
===================

Processed Variable or 'PV' is a term used by Yamcs Studio that covers the different types of data sources that a widget can be connected to. It is a more general term than parameter, which is a Yamcs Server notion.

PVs are uniquely identified by a *PV Name*. If multiple widgets have dependencies on the same PV, only one instance will be created and shared between these widgets.

The term PV is used to indicate both the name of a specific data source definition, as well as any instances of that definition. Context usually makes it apparent which of the two is meant.

A PV is considered *connected* if the data source is available, and at least one widget within Yamcs Studio is subscribing to it. As soon as no more widgets are connected to a PV, the PV gets *disconnected*.


.. toctree::
    :maxdepth: 1

    loc
    formulas
    para
    sim
    state
    sys
