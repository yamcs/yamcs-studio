var Axis = Java.type("org.eclipse.nebula.visualization.xygraph.figures.Axis");

var xygraph = widget.getFigure().getXYGraph();

for(var i=0; i<5; i++){
	var axis = new Axis("Axis", true);
	axis.setPrimarySide(i%2==0);
	axis.setRange(-10-10*i, 50-10*i);
	var trace =xygraph.getPlotArea().getTraceList().get(i);
	axis.setForegroundColor(trace.getTraceColor());
	xygraph.addAxis(axis);
	trace.setYAxis(axis);
}
