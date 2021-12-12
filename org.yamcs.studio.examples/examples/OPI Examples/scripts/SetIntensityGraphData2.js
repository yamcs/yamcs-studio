var UIBundlingThread = Java.type("org.csstudio.ui.util.thread.UIBundlingThread");
var Display = Java.type("org.eclipse.swt.widgets.Display");
var Runnable = Java.type("java.lang.Runnable");
var Thread = Java.type("java.lang.Thread");

var currentDisplay = Display.getCurrent();

var thread = new Thread(new Runnable() {
	run: function() {
		var simuData = [];
		var value = PVUtil.getDouble(pvs[0]);
		var dataSrc = PVUtil.getString(pvs[1]);
		
		if (dataSrc == "Linear Sine Wave") {
			for (var i = 0; i < 256; i++) {
				for (var j = 0; j < 256; j++) {
					simuData[i * 256 + j] = Math.sin(j * 6 * Math.PI / 256 + i * 6 * Math.PI / 256 + value);
				}
			}
		} else {
			for (var i = 0; i < 256; i++) {
				for (var j = 0; j < 256; j++) {
					var x = j - 128;
					var y = i - 128;
					var p = Math.sqrt(x * x + y * y);
					simuData[i*256 + j] = Math.sin(p * 2 * Math.PI / 256 + value);
				}
			}
		}
		
		UIBundlingThread.getInstance().addRunnable(currentDisplay, new Runnable() {
			run: function() {
				widget.setValue(Java.to(simuData, "double[]"));
			}
		})
	}
});
thread.start();
