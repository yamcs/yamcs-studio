Local PVs
=========

Local PVs are read and written entirely in a running Yamcs Studio instance. They are never communicated to Yamcs, nor to any other copies of Yamcs Studio. Local PVs are typically used by the display author as a means to store information that needs to be communicated from one widget to another, or indeed from one display to another. They are very useful when scripting advanced displays.

Local PVs are transient, and are reset when Yamcs Studio is restarted. Local PVs do not need to be specially created. They are automatically instantiated when needed.

Example PV Names:

* ``loc://foo``
* ``loc://my-favourite-local-pv``
* ``loc://anything-you-want-really``

You can assign an initial value to a local PV by adding it after its name. For example:

* ``loc://foo(1)``
* ``loc://bar("abc")``
* ``loc://baz(1, 2, 3, 4, 5)``

A local PV can be configured to enforce a specific type by specifying one of ``VDouble``, ``VString``, ``VDoubleArray``, ``VStringArray``, ``VTable`` or ``VEnum`` in angle brackets. For example, a control widget with ``PV Name`` set to ``loc://foo<VString>`` will emit string values, even if the input is numeric.

To combine a type and an initializer use this syntax:

* ``loc://foo<VString>("200")``
* ``loc://foo<VDouble>(200)``
