Example Action Telecommand
==========================

These section presents two examples showing how to send a Yamcs telecommand from an Action button in an OPI display.

The first example shows how to send a simple predefined telecommand, which would have been generated from the command stack.

The second example shows how to retrieve a telecommand argument from another widget in the display and to send the telecommand.


.. rubric:: Simple telecommand

#. Create a telecommand in the view "Command Stack". Right click and select "Copy". The telecommand source is saved in the clipboard and will be used when creating the action button.

   .. image:: _images/command-source.png
       :alt: Retrieve Telecommand Source
       :align: center

#. In the display builder, create an Action button. Add an action of type **Javascript**, activate the property **Embedded**.

#. In the window **Edit Script Text**, enter the instruction ``Yamcs.issueCommand("")``, and paste the content of the clipboard insert the command source as a string. The result should be similar to:

   .. image:: _images/action-telecommand.png
       :alt: Action Telecommand
       :align: center

#. Save the display. In the Display Runner, refresh the display and try the new action button. A command is inserted in the `<../views/command-history>`_ view with the defined command source content.


.. rubric:: Retrieve telecommand argument from another widget

#. Create a dropdown widget in the display. Give it a name (e.g. *ComboVoltageNumber*) and a local pv name (e.g. *loc://voltage_number*) to store the selection.

   .. image:: _images/telecommand-combo-argument.png
       :alt: Telecommand Combo
       :align: center

#. Create the action button as in example 1, and add the following script to retreive the pv value from the combo box, and append it to the telecommand source:

   .. code:: javascript

       var voltage_number = parseInt(display.getWidget('ComboVoltageNumber').getValue());
       Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON', {
         'voltage_num': voltage_number
       });

   .. note::

       When the scripts get more complex, it is recommended to defined them in their own file to ease their edition and maintenance.

#. Save the display. In the Display Runner, refresh the display, select a value in the combo widget and try the action button. A command is inserted in the `<../views/command-history>`_ view with the selected voltage as argument.
