var SpreadSheetTable = Java.type("org.csstudio.swt.widgets.natives.SpreadSheetTable");

var table = widget.getTable();

function updateLabel(content) {
	var text = "";
	for (var i = 0; i < content.length; i++) {
		var row = content[i];
		for (var j = 0; j < row.length; j++) {
			text += row[j];
			if (j != row.length - 1) {
				text += ", ";
			}
		}
		text = text + "\n"
	}
	display.getWidget("contentLabel").setPropertyValue("text", text);
}

updateLabel(table.getContent());

table.addModifiedListener(new SpreadSheetTable.ITableModifiedListener({
	modified: function(content) {
		updateLabel(content);
	}
}));
