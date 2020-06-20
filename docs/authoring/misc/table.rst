Table
=====

Advanced widget for structuring tabular data. Does not connect to PVs by itself. Its main use comes when combined with scripts.

.. image:: _images/table.png
    :alt: Table
    :align: center

For example, the following snippet would append and reveal a row to an empty table widget named `mytable`:

.. code-block:: javascript

    var table = display.getWidget('mytable').getTable();

    var rowIndex = table.appendRow();
    table.setCellText(rowIndex, 0, Math.random());
    table.revealRow(rowIndex);


You can also directly set any row or column cell text. If absent, missing rows and columns are created automatically:

.. code-block:: javascript

    var table = display.getWidget('mytable').getTable();
    table.setCellText(4, 5, Math.random());
