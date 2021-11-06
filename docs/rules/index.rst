Rules
=====

Widgets have many static properties. With *rules* we can modify these
properties dynamically, for example based on the incoming value updates
of a Parameter PV.

Rules are simpler to create than :doc:`../scripts/index`, but offer only
a subset of dynamic functionality. In fact, under the hood rules
are automatically converted to a script.

Assume that want to make an LED square when it is off, and round when
it is on. The static properties would not allow for such behaviour,
so we add a rule.

#. Click the **Rules** property fo the LED to pop up this dialog.

   .. image:: _images/attach-rules.png
       :alt: Attach a Rule
       :align: center

#. Click the plus icon, which opens this dialog:

   .. image:: _images/edit-rule.png
       :alt: Edit Rule
       :align: center

#. First choose the rule's target **Property**. Select **Square LED**.

#. In the right **Input PVs** table add your input PV. In this example we
   choose to generate an alternating 0/1 value using a
   :doc:`simulated PV <../pv/sim>`. Notice the sequential number in the
   ``#`` column. The first PV is numbered ``0``. Make sure to check the
   ``Trigger`` checkbox as this will then trigger the execution of the
   rule whenever the PV's value is updated.

#. In the **Expressions** table, fill in the conditions in the
   **Boolean Expression** column, and add the matching value of the
   rule's property in the **Output Value** column. The double value
   of the top-most right PV is available as the variable ``pv0``.
   The next PV in the list (if applicable) is available as the variable
   ``pv1``, etc.

   .. image:: _images/rule-example.png
       :alt: Example Rule
       :align: center

#. Confirm all dialogs, save your display and refresh a runtime view of it.
   You should see the LED's shape now alternating between square and round.


.. rubric:: Boolean Expression

This input field needs to be expressed in JavaScript. The **Input PVs** are
available in different formats:

.. list-table::
    :header-rows: 1
    :widths: 25 50

    * - Type
      - Example
    * - Double Value
      - ``pv0 == 2.2``
    * - String Value
      - ``pvStr0 != 'abc'``
    * - Integer Value
      - ``pvInt0 >= 2``

The numeric alarm state of an input PV is is accessible as follows:

.. list-table::
    :header-rows: 1
    :widths: 25 50

    * - Alarm
      - Example
    * - Invalid
      - ``pvSev0 == -1``
    * - No Alarm
      - ``pvSev0 == 0``
    * - Minor Alarm
      - ``pvSev0 == 2``
    * - Major Alarm
      - ``pvSev0 == 1``

.. note::

    If you wish to set a property value that always applies, use ``true`` (or ``1==1``) as the Boolean Expression.


.. rubric:: Output Value

The exact form that the **Output Value** column adopts depends on the type of the property. Some properties are colors, so you would see a color picker, other properties expect text, and the above example was a boolean yes/no, so we got a checkbox.
