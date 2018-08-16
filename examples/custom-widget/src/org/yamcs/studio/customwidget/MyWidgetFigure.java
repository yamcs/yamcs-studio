package org.yamcs.studio.customwidget;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;

public class MyWidgetFigure extends Figure {

    private double min = 0;
    private double max = 100;
    private double value = 50;

    @Override
    protected void paintClientArea(Graphics graphics) {
        super.paintClientArea(graphics);

        graphics.setBackgroundColor(getBackgroundColor());
        graphics.fillRectangle(getClientArea());

        graphics.setBackgroundColor(getForegroundColor());

        // Coerce drawing value in range
        double coercedValue = value;
        if (value < min) {
            coercedValue = min;
        } else if (value > max) {
            coercedValue = max;
        }
        int valueLength = (int) ((coercedValue - min) * getClientArea().height / (max - min));
        graphics.fillRectangle(getClientArea().x,
                getClientArea().y + getClientArea().height - valueLength,
                getClientArea().width, valueLength);
    }

    public void setMin(double min) {
        this.min = min;
        repaint();
    }

    public void setMax(double max) {
        this.max = max;
        repaint();
    }

    public void setValue(double value) {
        this.value = value;
        repaint();
    }

    public double getValue() {
        return value;
    }
}
