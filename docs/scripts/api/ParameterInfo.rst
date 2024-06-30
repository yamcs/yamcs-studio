ParameterInfo
=============

ParameterInfo objects are returned from the call ``Yamcs.getParameterInfo(pv)``.

.. versionadded:: 1.7.5

**name**
    Short name of this parameter (relative to its parent system).

**qualifiedName**
    Fully qualified name of this parameter.

**shortDescription**
    Short description for this parameter.

**longDescription**
    Long description for this parameter.

**dataSource**
    Source of values for this parameter.

**units**
    Units for this parameter.

**type**
    Engineering type of values, as specified by the MDB.

**rawType**
    Raw type of values, as specified by the MDB.

**getAlias(** namespace **)**
    Returns the alias for this parameter, for a given namespace.
