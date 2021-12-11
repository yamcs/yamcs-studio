var Runnable = Java.type("java.lang.Runnable");
var Thread = Java.type("java.lang.Thread");

new Thread(new Runnable({
	run: function() {
		for (var i = 5; i > 0; i--) {
			if (!display.isActive()) {
				return;
			}
			Thread.sleep(1000);
		}
		pvs[1].setValue(PVUtil.getLong(pvs[0]));
	}
})).start();
