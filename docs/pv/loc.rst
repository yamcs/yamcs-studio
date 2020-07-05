Local PVs
=========

Local PVs are read and written entirely in a running Yamcs Studio instance. They are never communicated to Yamcs, nor to any other copies of Yamcs Studio. Local PVs are typically used by the display author as a means to store information that needs to be communicated from one widget to another. They also form a powerful building block when scripting advanced displays due to their ability to store runtime state. This makes it possible to script logic based on a historical window of values.

Local PVs are transient, and are reset when Yamcs Studio is restarted. Local PVs do not need to be specially created. They are automatically instantiated when needed.

Example PV Names:

* ``loc://foo``
* ``loc://my-favourite-local-pv``
* ``loc://anything-you-want-really``

You can assign an initial value to a local PV by adding it after its name. For instance:

* ``loc://foo(1)``
* ``loc://bar("abc")``
