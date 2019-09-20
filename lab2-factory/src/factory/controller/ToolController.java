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
            togglePressing();
            conveyor.off();
            press.on();
            wait(pressingMillis);
            //Thread.sleep(pressingMillis);
            press.off();
            wait(pressingMillis);
            //Thread.sleep(pressingMillis);
            togglePressing();
            startBelt();
            //conveyor.on();
        }
    }

    public synchronized void onPaintSensorHigh(WidgetKind widgetKind) throws InterruptedException {
        if (widgetKind == WidgetKind.ORANGE_ROUND_WIDGET) {
            togglePainting();
            conveyor.off();
        	paint.on();
        	wait(paintingMillis);
            //Thread.sleep(paintingMillis);
        	paint.off();
            togglePainting();
            startBelt();
            //conveyor.on();
        }
    }

    private void startBelt() throws InterruptedException {
//        while (painting || pressing) {
//            wait();
//        }

        if (!painting && !pressing) conveyor.on();
    }

    private void togglePainting() {
        painting = !painting;
        //notifyAll();
    }

    private void togglePressing() {
        pressing = !pressing;
        //notifyAll();
    }

    public static void main(String[] args) {
        Factory factory = new Factory();
        factory.startSimulation();
    }
}
