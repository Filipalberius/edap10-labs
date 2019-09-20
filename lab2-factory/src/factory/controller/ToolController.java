package factory.controller;

import factory.model.DigitalSignal;
import factory.model.WidgetKind;
import factory.swingview.Factory;

public class ToolController {
    private final DigitalSignal conveyor, press, paint;
    private final long pressingMillis, paintingMillis;
    private boolean painting = false;
    private boolean pressing = false;
    
    public ToolController(DigitalSignal conveyor,
                          DigitalSignal press,
                          DigitalSignal paint,
                          long pressingMillis,
                          long paintingMillis)
    {
        this.conveyor = conveyor;
        this.press = press;
        this.paint = paint;
        this.pressingMillis = pressingMillis;
        this.paintingMillis = paintingMillis;
    }

    public synchronized void onPressSensorHigh(WidgetKind widgetKind) throws InterruptedException {
        if (widgetKind == WidgetKind.BLUE_RECTANGULAR_WIDGET) {
            pressing = true;
            conveyor.off();
            press.on();
            waitOutside(pressingMillis);
            press.off();
            waitOutside(pressingMillis);
            pressing = false;
            startBelt();
        }
    }

    public synchronized void onPaintSensorHigh(WidgetKind widgetKind) throws InterruptedException {
        if (widgetKind == WidgetKind.ORANGE_ROUND_WIDGET) {
            painting = true;
            conveyor.off();
        	paint.on();
        	waitOutside(paintingMillis);
        	paint.off();
            painting = false;
            startBelt();
        }
    }

    private void startBelt() {
        if (!(painting || pressing)) {
            conveyor.on();
        }
    }

    /** Helper method: sleep outside of monitor for ’millis’ milliseconds. */
    private void waitOutside(long millis) throws InterruptedException {
        long timeToWakeUp = System.currentTimeMillis() + millis;

        while (System.currentTimeMillis() < timeToWakeUp) {
            long dt = timeToWakeUp - System.currentTimeMillis();
            wait(dt);
        }
    }

    public static void main(String[] args) {
        Factory factory = new Factory();
        factory.startSimulation();
    }
}
