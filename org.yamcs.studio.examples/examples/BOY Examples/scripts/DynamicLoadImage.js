var value = PVUtil.getDouble(pvArray[0]);

if (value==0) {
	widgetController.setPropertyValue("image_file", "../pictures/fish.gif");
} else if(value == 1) {
	widgetController.setPropertyValue("image_file", "https://www.google.com/images/srpr/logo11w.png");
} else if(value == 2) {
	widgetController.setPropertyValue("image_file", "/BOY Examples/widgets/DynamicSymbols/Shy.jpg");
} else if(value == 3) {
	widgetController.setPropertyValue("image_file", "/BOY Examples/widgets/DynamicSymbols/Haha.jpg");
}
