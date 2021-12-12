var IntensityGraphFigure = Java.type("org.csstudio.swt.widgets.figures.IntensityGraphFigure");

widget.getFigure().addPixelInfoProvider(new IntensityGraphFigure.IPixelInfoProvider({
	/*
	 * Provide custom information for each pixel.
	 * For example the related geometry information on a pixel.
	 */
	getPixelInfo: function(xIndex, yIndex, xCoordinate, yCoordinate, pixelValue) {
		return "\nMy index is (" + xIndex + ", " + yIndex + " )";
	}
}));
