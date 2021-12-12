var IPVListener = Java.type("org.yamcs.studio.data.IPVListener");
var Display = Java.type("org.eclipse.swt.widgets.Display");
var Annotation = Java.type("org.eclipse.nebula.visualization.xygraph.figures.Annotation");
var IAnnotationListener = Java.type("org.eclipse.nebula.visualization.xygraph.figures.IAnnotationListener");
var Runnable = Java.type("java.lang.Runnable");
var Thread = Java.type("java.lang.Thread");

var leftPV = pvs[1];
var rightPV = pvs[2];

var xyGraph = widget.getFigure().getXYGraph();

var leftAnnotation = new Annotation("Left", xyGraph.primaryXAxis, xyGraph.primaryYAxis);
leftAnnotation.setCursorLineStyle(Annotation.CursorLineStyle.UP_DOWN);
xyGraph.addAnnotation(leftAnnotation);

leftAnnotation.addAnnotationListener(new IAnnotationListener() {
	annotationMoved: function(oldX, oldY, newX, newY) {
		leftPV.setValue(newX);
	}
});
leftAnnotation.setValues(2, 5);

var rightAnnotation = new Annotation("Right", xyGraph.primaryXAxis, xyGraph.primaryYAxis);
rightAnnotation.setCursorLineStyle(Annotation.CursorLineStyle.UP_DOWN);
rightAnnotation.setValues(7, 5);
xyGraph.addAnnotation(rightAnnotation);

rightAnnotation.addAnnotationListener(new IAnnotationListener() {
	annotationMoved: function(oldX, oldY, newX, newY) {
		rightPV.setValue(newX);
	}
});

var currentDisplay = Display.getCurrent();

var updateAnnotationPVListener = new IPVListener() {
	valueChanged: function(pv) {
		currentDisplay.asyncExec(new Runnable() {
			run: function() {
				// this method must be called in UI thread
				leftAnnotation.setValues(PVUtil.getDouble(leftPV), leftAnnotation.getYValue());
				rightAnnotation.setValues(PVUtil.getDouble(rightPV), rightAnnotation.getYValue());
			}
		});
	}
};

leftPV.addListener(updateAnnotationPVListener);
rightPV.addListener(updateAnnotationPVListener);
