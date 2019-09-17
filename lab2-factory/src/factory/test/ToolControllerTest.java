package factory.test;

import factory.model.WidgetKind;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ToolControllerTest {

    /** Test that the press is not lowered while the motor is running. */
    @Test
    public void noMoveDuringPress() throws InterruptedException {
        MockFactory factory = new MockFactory(200, 100);
        CheckedSignal motor = new CheckedSignal(factory.press);
        factory.motor = motor;
        factory.start();

        // Queue up items.
        factory.queueItem(WidgetKind.ORANGE_ROUND_WIDGET);
        factory.queueItem(WidgetKind.BLUE_RECTANGULAR_WIDGET);

        factory.triggerPressSensor(); // Simulate first item passing the press (blocking).
        // Concurrently paint/press:
        Thread press = new Thread(factory::triggerPressSensor);
        Thread paint = new Thread(factory::triggerPaintSensor);
        press.start();
        paint.start();
        press.join();
        paint.join();

        assertEquals("Motor was running while the press was active!", 0, motor.hiOnEnable());
        assertEquals("Motor was running while the press was active!", 0, motor.hiOnDisable());
    }

    /** Same as noMoveDuringPress but high press/paint frequency. */
    @Test
    public void noMoveDuringPressFast() throws InterruptedException {
        MockFactory factory = new MockFactory(0, 0);
        CheckedSignal motor = new CheckedSignal(factory.press);
        factory.motor = motor;
        factory.start();
        int N = 20000;

        Thread press = new Thread(() -> {
            try {
                for (int i = 0; i < N; ++i) {
                    factory.triggerPressSensor(WidgetKind.BLUE_RECTANGULAR_WIDGET);
                }
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        });
        Thread paint = new Thread(() -> {
            try {
                for (int i = 0; i < N; ++i) {
                    factory.triggerPaintSensor(WidgetKind.ORANGE_ROUND_WIDGET);
                }
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        });
        press.start();
        paint.start();
        press.join();
        paint.join();

        assertEquals("Motor was running while the press was active!", 0, motor.hiOnEnable());
        assertEquals("Motor was running while the press was active!", 0, motor.hiOnDisable());
    }

    /** Test that the hydraulic press does not complete pressing faster than required. */
    @Test
    public void pressingTakesTime() {
        long pressDelay = 200;
        long cycleTime = pressDelay * 2;
        MockFactory factory = new MockFactory(pressDelay, 100);
        factory.start();

        factory.queueItem(WidgetKind.BLUE_RECTANGULAR_WIDGET);

        long t0 = System.currentTimeMillis();
        factory.triggerPressSensor();
        long delta = System.currentTimeMillis() - t0;
        assertTrue("Pressing finished too soon.", delta >= cycleTime);
    }

}
