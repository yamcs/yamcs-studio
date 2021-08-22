PV
==

PV instances belong to a single widget. The PV Name identifies what data
it represents, and may come from various different data sources.

**getName()**
    Returns the PV Name.

**getValue()**
    Returns the current value of this PV.

**setValue(** value **)**
    Write a value to this PV. This method
    returns immediately.

**setValue(** value, timeout **)**
    Write a value to this PV and block until
    either the write was successful, or the
    timeout (in milliseconds) was reached.

    Returns true if the write was successful.

**start()**
    Connect and start listening to updates. It is not allowed
    to start an already started PV.

**stop()**
    Disconnect a PV. Its listeners are preserved for a potential
    future restart.

**addListener(** listener **)**
    Add an IPVListener implementation for receiving updates. Example:

    .. code-block:: javascript

        var IPVListener = Java.type("org.yamcs.studio.data.IPVListener");
        myPV.addListener(new IPVListener({
            valueChanged: function(pv) {
                ConsoleUtil.writeInfo("An update: " + PVUtil.getString(pv));
            }
        }));

    .. note::
        
        In most circumstances it would be preferrable to add
        the PV of interest as a trigger to the Input PVs of a
        particular script, so that value updates can more simply
        be accessed from the global ``pvs`` array.

**removeListener(** listener **)**
    Stop an IPVListener implementation from receiving updates.

**isConnected()**
    Returns true if this PV is currently *connected*. What this
    means depends on the underlying datasource. For example, if
    it concerns a Yamcs parameter, *connected* means that the
    WebSocket subscription is up and running.

**isWriteAllowed()**
    Returns true if this PV may be used in control widgets.

    PVs for Yamcs parameters accept all writes, and leave the finally
    decision to Yamcs itself.
