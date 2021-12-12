var SpreadSheetTable = Java.type("org.csstudio.swt.widgets.natives.SpreadSheetTable");

var table = widget.getTable();

table.addSelectionChangedListener(new SpreadSheetTable.ITableSelectionChangedListener({
	selectionChanged: function(selection) {
		var text = "";
		for (var i = 0; i < selection.length; i++) {
			var row = selection[i];
			for (var j = 0; j < row.length; j++) {
				text += row[j];
				if (j != row.length - 1) {
					text += ", ";
				}
			}
			text = text + "\n";
		}
		display.getWidget("selectionLabel").setPropertyValue("text", text);
	}
}));
