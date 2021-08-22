Table
=====

Widget that works like a spreadsheet.

.. image:: _images/examples/table.png
    :alt: Table
    :align: center

Not all functionalities are exposed as properties. The main use case for
this widget is to be populated dynamically from within scripts.

For example, the following JavaScript would append and reveal a
row. To make the script run upon display initialization, attach it to
the Table widget with a trigger PV set to the formula ``=1``.

.. code-block:: javascript

    var table = widget.getTable();

    var rowIndex = table.appendRow();
    table.setCellText(rowIndex, 0, Math.random());
    table.revealRow(rowIndex);

Any row column can be modified. The table is automatically extended
as needed:

.. code-block:: javascript

    var table = widget.getTable();
    table.setCellText(4, 5, Math.random());


..
    ---------------------------------------------------------------------------
.. rubric:: Basic Properties

.. include:: _props/name.rst
.. include:: _props/widget_type.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Behavior Properties

.. include:: _props/actions.rst

Editable (``editable``)
    Same as **Enabled**, but if unset, cells can still be selected.


Enabled (``enabled``)
    If set, every cell can be modified at runtime by clicking on it. Rows
    and columns can be managed by using the right-click context menu
    available from table content.

    If unset, cells can't be modified, nor can anything be selected.

.. include:: _props/rules.rst
.. include:: _props/scripts.rst
.. include:: _props/visible.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Border Properties

.. include:: _props/border_color.rst
.. include:: _props/border_style.rst
.. include:: _props/border_width.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Display Properties

.. include:: _props/background_color.rst

Column Headers (``column_headers``)
    Configure the following properties for each column: **Column Title**,
    **Column Width**, **Editable** and **CellEditor**.

    **CellEditor** is one of ``TEXT``, ``DROPDOWN``, ``CHECKBOX`` or
    ``CUSTOMIZED``.

    In case of ``DROPDOWN``, the available items should be set through
    scripting. Here for column 2:

    .. code-block:: javascript

        table = widget.getTable();
        var options = Java.to(["Abc", "Def", "Ghi"], "java.lang.String[]");
        table.setColumnCellEditorData(2, options);
    
    In case of ``CHECKBOX``, the boolean labels default to "Yes" and "No".
    These values can be customized through scripting. Here for column 2:

    .. code-block:: javascript

        table = widget.getTable();
        var options = Java.to(["ON", "OFF"], "java.lang.String[]");
        table.setColumnCellEditorData(2, ["ON", "OFF"]);
    
    In case of ``CUSTOMIZED`` you must provide a custom ``CellEditor``.
    This is an advanced use case and not further detailed.


Column Headers Visible (``column_headers_visible``)
    Unset to hide column headers.

Columns Count (``columns_count``)
    Number of columns.

Default Content (``default_content``)
    Optional initial table data.

.. include:: _props/foreground_color.rst
.. include:: _props/tooltip.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Position Properties

.. include:: _props/height.rst
.. include:: _props/scale_options.rst
.. include:: _props/width.rst
.. include:: _props/x.rst
.. include:: _props/y.rst


..
    ---------------------------------------------------------------------------
.. rubric:: Additional API

Table widgets expose the following additional :doc:`../scripts/api/Widget`
API for use in scripting:

**getTable()**
    Returns a SpreadSheetTable object for modifying the spreadsheet underlying this table.

**setAllowedHeaders(** headers **)**
    Restrict the column names to the given string array.
    If this is set, adding a column at runtime will
    require to select from one of the available headers.


..
    ---------------------------------------------------------------------------
.. rubric:: Class: ``SpreadSheetTable``

**addCellEditingListener(** listener **)**
    Adds a listener that gets notified whenever a cell is edited.

    Usage with JavaScript is as follows:

    .. code-block:: javascript

        var SpreadSheetTable = Java.type(
            "org.csstudio.swt.widgets.natives.SpreadSheetTable");

        var table = widget.getTable();
        table.addCellEditingListener(
            new SpreadSheetTable.ITableCellEditingListener({
                cellValueChanged: function(row, col, oldValue, newValue) {
                    ConsoleUtil.writeInfo("A cell was modified");
                }
            })
        );

**addModifiedListener(** listener **)**
    Adds a listener that gets notified whenever content is modified.

    Usage with JavaScript is as follows:

    .. code-block:: javascript

        var SpreadSheetTable = Java.type(
            "org.csstudio.swt.widgets.natives.SpreadSheetTable");

        var table = widget.getTable();
        table.addModifiedListener(
            new SpreadSheetTable.ITableModifiedListener({
                modified: function(content) {
                    ConsoleUtil.writeInfo("Content was modified");
                }
            })
        );
    
**addSelectionChangedListener(** listener **)**
    Adds a listener that gets notified whenever the selection of the table
    changes.

    Usage with JavaScript is as follows:

    .. code-block:: javascript

        var SpreadSheetTable = Java.type(
            "org.csstudio.swt.widgets.natives.SpreadSheetTable");

        var table = widget.getTable();
        table.addSelectionChangedListener(
            new SpreadSheetTable.ITableSelectionChangedListener({
                selectionChanged: function(selection) {
                    ConsoleUtil.writeInfo("Selection has changed");
                }
            })
        );

**appendRow()**
    Adds a row at the bottom of to the spreadsheet, returning the row index.

**autoSizeColumns()**
    Calculate and apply automatic widths for all columns.

**deleteColumn(** index **)**
    Deletes a column.

**deleteRow(** index **)**
    Deletes a row.

**getCellText(** row, col **)**
    Returns the text of a specific cell.

**getColumnCount()**
    Returns the number of columns.

**isColumnEditable(** index **)**
    Returns ``true`` if a column is editable.

**getColumnHeaders()**
    Returns an array with the column headers.

**getContent()**
    Returns all spreadsheet data as a two-dimensional array.

**getRowCount()**
    Returns the number of rows.

**getSelection()**
    Returns the current selected data as a two-dimensional array.

**insertColumn(** index **)**
    Insert a new column, where each value is initialized to
    an empty string.

**insertRow(** index **)**
    Insert a new row, where each value is initialized to
    an empty string.

**isEditable()**
    Returns ``true`` if the spreadsheet is editable.

**isEmpty()**
    Returns ``true`` if the spreadsheet is empty.

**refresh()**
    Refresh the table to reflect its content.

**revealRow(** index **)**
    Scroll a specific row into view.

**setCellBackground(** row, col, color **)**
    Set the background color of a cell.

    Colors can be obtained from :doc:`../scripts/api/ColorFontUtil`.

**setCellForeground(** row, col, color **)**
    Set the foreground color of a cell.

    Colors can be obtained from :doc:`../scripts/api/ColorFontUtil`.

**setCellText(** row, col, text **)**
    Set the text of a cell. If the row index is
    beyond the current row count, the spreadsheet is
    extended as necessary.

**setColumnCellEditorData(** col, data **)**
    Set data required for a specific Cell Editor.

    In the case of a Cell Editor of type ``DROPDOWN``, data should
    be a Java array of strings. In JavaScript this can be created
    like this:

    .. code-block:: javascript

        var data = Java.to(["Abc", "Def", "Ghi"], "java.lang.String[]");

    In the case of a Cell Editor of type ``CHECKBOX``, data should
    be an array of two strings. One representing the on label, and one
    representing the off label. In JavaScript this can be created
    like this:

    .. code-block:: javascript

        var data = Java.to(["ON", "OFF"], "java.lang.String[]");

**setColumnCellEditorType(** col, type **)**
    Set the editor for cells of a specific column. Type must be one of
    ``TEXT``, ``DROPDOWN``, ``CHECKBOX`` or ``CUSTOMIZED``.

**setColumnEditable(** col, editable **)**
    Set whether the given column is editable or not.

**setColumnHeader(** col, header **)**
    Set the header for a specific column.

**setColumnHeaders(** headers **)**
    Set multiple column headers at once. If the given array is
    larger than the current column count, new columns will be
    appended to the right.

**setColumnHeaderVisible(** show **)**
    Set whether to show headers or not.

**setColumnsCount(** count **)**
    Set the number of columns. If the number is less than the current
    number of columns, columns will be deleted from the right. If the
    number is greater than the current number of columns, new columns
    will be appended to the right.

**setColumnWidth(** col, width **)**
    Set the pixel width of a specific column.

**setColumnWidths(** widths **)**
    Set multiple column widths at once. If the given array is
    larger than the current column count, new columns will be
    appended to the right.

**setContent(** content **)**
    Replace the current contents (a two-dimensional array)
    of this spreadsheet with the given content.

**setEditable(** editable **)**
    Set whether this spreadsheet is editable or not.

**setFont(** font **)**
    Set the font used in this spreadsheet.

    Fonts can be obtained from :doc:`../scripts/api/ColorFontUtil`.

**setRowBackground(** row, color **)**
    Set the background color of a row.

    Colors can be obtained from :doc:`../scripts/api/ColorFontUtil`.

**setRowForeground(** row, color **)**
    Set the foreground color of a column.

    Colors can be obtained from :doc:`../scripts/api/ColorFontUtil`.
