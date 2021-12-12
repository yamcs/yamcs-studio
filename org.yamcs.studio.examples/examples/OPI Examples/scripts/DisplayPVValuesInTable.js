var table = widget.getTable();

// Fill PV Name only once
if (!widget.getVar("ranBefore")) {
	widget.setVar("ranBefore", true);
	for (var i = 0; i < pvs.length; i++) {
		var pv = pvs[i];
		table.setCellText(i, 0, pv.getName());
		if (!pv.isConnected()) {
			table.setCellText(i, 1, "Disconnected");
		}
	}
}

// Find index of the trigger PV
var i = 0;
while (triggerPV != pvs[i]) {
    i += 1;
}

table.setCellText(i, 1, PVUtil.getString(triggerPV))
table.setCellText(i, 2, PVUtil.getTimeString(triggerPV))
table.setCellText(i, 3, PVUtil.getStatus(triggerPV))
table.setCellText(i, 4, PVUtil.getSeverityString(triggerPV))

var s = PVUtil.getSeverity(triggerPV);
if (s == 0) {
    table.setCellBackground(i, 4, ColorFontUtil.GREEN);
} else if (s == 1) {
    table.setCellBackground(i, 4, ColorFontUtil.RED);
} else if (s == 2) {
    table.setCellBackground(i, 4, ColorFontUtil.YELLOW);
} else if (s == 3) {
    table.setCellBackground(i, 4, ColorFontUtil.PINK);
} else {
	table.setCellBackground(i, 4, ColorFontUtil.WHITE);
}
