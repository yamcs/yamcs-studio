Rules
=====

Making OPI displays is flexible using the palette  and the :doc:`properties`, but the resulting displays are still fairly static. But what if we want to make the position of a widget dynamic based on a PV? Or if we want to dynamically change widget colors?

Every widget has a **Rules** property. Rules are a user-friendly way for adding dynamic behaviour to widgets. They are most often used for changing the widget's properties at runtime.


.. rubric:: Example

Suppose we want to make an LED square when it is off, and round when it is on. The static properties would not allow for such a scenario, we therefore add a rule.

#. Edit the **Rules** property to pop up this dialog.

   .. image:: _images/attach-rules.png
       :alt: Attach a Rule
       :align: center

#. Clicking the plus icon gives you this dialog:

   .. image:: _images/edit-rule.png
       :alt: Edit Rule
       :align: center

#. The first thing to choose is the rule's target **Property**. So select **Square LED**.

#. In the right **Input PVs** table add your input PV. In this example we chose to generate an alternating 0/1 value using a simulated PV. Notice the sequential number in the ``#`` column. The first PV is numbered ``0``. Make sure to check the ``Trigger`` checkbox as this will then trigger the execution of the rule whenever the PV's value is updated.

#. Now in the **Expressions** table, fill in your conditions in the **Boolean Expression** column, and add the desired value of the rule's property in the **Output Value** column. The double value of the top-most right PV is available as the variable ``pv0``. The next PV in the list (if applicable) is available as the variable ``pv1``, etc.

   .. image:: _images/rule-example.png
       :alt: Example Rule
       :align: center

#. Confirm your dialogs, save your display and refresh a runtime view of it. You should see the LED's shape now alternating between square and ground.

One can see that this example could be made arbitrarily complex by adding more rules and/or expressions.


.. rubric:: Boolean Expression

This input field needs to be expressed in JavaScript. The **Input PVs** are available in different formats:

.. list-table::
    :widths: 25 50

    * - Type
      - Example
    * - Double Value
      - ``pv0 == 2.2``
    * - String Value
      - ``pvStr0 != 'abc'``
    * - Integer Value
      - ``pvInt0 >= 2``

In addition, you can access the numeric alarm state of an input PV.

.. list-table::
    :widths: 25 50

    * - Alarm
      - Example
    * - Invalid
      - ``pvSev0 == -1``
    * - No Alarm
      - ``pvSev0 == 0``
    * - Minor Alarm
      - ``pvSev0 == 1``
    * - Major Alarm
      - ``pvSev0 == 2``

.. note::

    If you wish to set a property value that always applies, use ``true`` (or ``1==1``) as the Boolean Expression.


.. rubric:: Output Value

The exact form that the **Output Value** column adopts depends on the type of the property. Some properties are colors, so you would see a color picker, other properties expect text, and the above example was a boolean yes/no, so we got a checkbox.
