package lab;
import wash.WashingIO;

public class TemperatureController extends MessagingThread<WashingMessage> {

    private WashingIO io;

    TemperatureController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {

        double targetTemp = 0;
        int state = 0;
        boolean changeTemp = false;
        MessagingThread<WashingMessage> sender = null;

        try {
            while (true) {
                // wait for up to a (simulated) minute for a WashingMessage
                WashingMessage m = receiveWithTimeout(10000 / Wash.SPEEDUP);

                if (m != null) {
                    System.out.println("got " + m);
                    state = m.getCommand();
                    if(state == 5) {
                        targetTemp = m.getValue();
                        changeTemp = true;
                    } else if (state == 4) {
                        io.heat(false);
                    }
                    sender = m.getSender();
                }

                if (state == 5) {
                    if (changeTemp) {
                        changeTemp = reachNewTemp(targetTemp, sender);
                    } else {
                        holdTemp(targetTemp);
                    }
                }
            }

        } catch (InterruptedException unexpected) {
            throw new Error(unexpected);
        }
    }

    private void holdTemp(double targetTemp) {
        if (io.getTemperature() > targetTemp - 1.478) {
            io.heat(false);
        } else {
            io.heat(true);
        }
    }

    private boolean reachNewTemp(double targetTemp, MessagingThread<WashingMessage> sender) {
        if (io.getTemperature() > targetTemp) {
            io.heat(false);
        } else {
            io.heat(true);
        }

        if (io.getTemperature() >= targetTemp - 2 && io.getTemperature() < targetTemp) {
            sender.send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
            return false;
        }
        return true;
    }
}
