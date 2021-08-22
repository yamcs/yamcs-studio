DataUtil
========

Helper methods for creating Java-compatible arguments.

**createDoubleArray(** size **)**
    Returns a new Java double array of the given size.

**createIntArray(** size **)**
    Returns a new Java int array of the given size.

**createMacrosInput(** include_parent_macros **)**
    Create a new ``MacrosInput`` object, useful when
    creating a container widget through scripting.

    **include_parent_macros** is a boolean value that indicates
    if the MacrosInput should inherit or start blank.

**toJavaDoubleArray(** array **)**
    Returns a Java array that matches the provided script
    array.

**toJavaIntArray(** array **)**
    Returns a Java array that matches the provided script
    array.

.. rubric:: Example

The following scripts do the same but in different ways:

.. code-block:: javascript

    // Create a Java array, then add to it
    var arr = DataUtil.createDoubleArray(100);
    for (var i = 0; i < 100; i++) {
        arr[i] = i;
    }
    pvs[0].setValue(arr);

.. code-block:: javascript

    // Create a JavaScript array, then convert it to Java    
    var arr = [];
    for (var i = 0; i < 100; i++) {
        arr[i] = i;
    }
    pvs[0].setValue(DataUtil.toJavaDoubleArray(arr));

Note that the same can actually also be achieved by not using
``DataUtil``, but instead the ``Java`` class available to
Nashorn scripts:

.. code-block:: javascript

    var arr = [];
    for (var i = 0; i < 100; i++) {
        arr[i] = i;
    }
    pvs[0].setValue(Java.to(arr, "double[]"));
