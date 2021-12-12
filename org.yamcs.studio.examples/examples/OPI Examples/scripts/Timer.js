var Runnable = Java.type("java.lang.Runnable");
var Thread = Java.type("java.lang.Thread");

var startButton = display.getWidget("Start_Button");
var stopButton = display.getWidget("Stop_Button");
var resetButton = display.getWidget("Reset_Button");
var bar = display.getWidget("Progress_Bar");

var hourText = display.getWidget("hourText");
var hourPV = hourText.getPV();
var minText = display.getWidget("minText");
var minPV = minText.getPV();
var secText = display.getWidget("secText");
var secPV = secText.getPV();

var timerLabel = display.getWidget("timerLabel");

if (PVUtil.getLong(pvs[0]) == 1) {
	var thread = new Thread(new Runnable({
		run: function() {
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
			resetButton.setEnabled(false);
			bar.setVisible(true);
			hourText.setEnabled(false);
			minText.setEnabled(false);
			secText.setEnabled(false);
			var hour = PVUtil.getLong(hourPV);
			var min = PVUtil.getLong(minPV);
			var sec = PVUtil.getLong(secPV);
			// remember the values to be reset
			resetButton.setVar("hour", hour);
			resetButton.setVar("min", min);
			resetButton.setVar("sec", sec);
			timerLabel.setPropertyValue("foreground_color", ColorFontUtil.BLACK);
			timerLabel.setPropertyValue("text", "Time Left:");
			var stopped = false;
			var total = hour * 3600 + min * 60 + sec;
			for (var i = total; i >= 0; i--) {
				if (!display.isActive()) {
					return;
				}
				if (PVUtil.getLong(pvs[0]) == 0) {
					stopped = true;
					break;
				}
				pvs[1].setValue(100 - 100 * i / total);
				hourPV.setValue(Math.floor(i / 3600));
				minPV.setValue(Math.floor(i % 3600 / 60));
				secPV.setValue(Math.floor(i % 60));
				Thread.sleep(1000);
			}
			    
			timerLabel.setPropertyValue("foreground_color", ColorFontUtil.RED);
			if (stopped) {
				timerLabel.setPropertyValue("text", "Interrupted!");
			} else {
				timerLabel.setPropertyValue("text", "Time's Up!!!");
				widget.executeAction(0);
				pvs[2].setValue(1);
				new Thread(new Runnable({
					run: function() {
						var i = 0;
						while (PVUtil.getLong(pvs[2]) == 1) {
							Thread.sleep(500);
							var color = (i % 2) ? ColorFontUtil.YELLOW : ColorFontUtil.RED;
							timerLabel.setPropertyValue("foreground_color", color);
							i = i + 1;
						}
						timerLabel.setPropertyValue("foreground_color", ColorFontUtil.BLACK);
					}
				})).start()
			}
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
			resetButton.setEnabled(true);
			bar.setVisible(false);
			hourText.setEnabled(true);
			minText.setEnabled(true);
			secText.setEnabled(true);
        }
    }));
	thread.start();
}
