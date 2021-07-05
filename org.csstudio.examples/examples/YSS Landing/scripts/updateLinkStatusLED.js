if (pvs[0].getValue() == null) {
    widget.setValue(1.0);
    widget.setPropertyValue("tooltip", "no value");
} else {
    var v = PVUtil.getString(pvs[0]);
    if (v === "DISABLED") {
        widget.setValue(1.0);
    } else if (v === "OK") {
        widget.setValue(2.0);
    } else {
        widget.setValue(3.0);
    }
    widget.setPropertyValue("tooltip", v);
}
