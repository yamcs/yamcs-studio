Example Update Widget Properties
================================

The script below is triggered from a PV that provide Strings with value *DISABLED*, *OK* or not *NOK*. The script retreives the value of the input PV as a string and sets the LED value accordingly. The tooltip value is also updated.

.. code:: javascript

    var v = PVUtil.getString(pvs[0]);
    if (v == 'DISABLED') {
        widget.setValue(1.0);
    } else if (v == 'OK') {
        widget.setValue(2.0);
    } else {
        widget.setValue(3.0);
    }

    widget.setPropertyValue('tooltip', v);


With the LED widget defined as pictured:

.. image:: _images/Example-script-led.png
    :alt: Action Telecommand
    :align: center
