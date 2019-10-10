package lab;
import wash.WashingIO;

public class TemperatureController extends MessagingThread<WashingMessage> {

    private WashingIO io;

    TemperatureController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {
        try {
            int command = 0;
            double value = 0;

            while (true) {
                // wait for up to a (simulated) minute for a WashingMessage
                WashingMessage m = receiveWithTimeout(10000 / Wash.SPEEDUP);

                if (m != null) {
                    System.out.println("got " + m);
                    command = m.getCommand();
                    value = m.getValue();
                }

                switch (command) {
                    case 4:
                        io.heat(false);
                        break;
                    case 5:
                        if (io.getTemperature() > value - 0.478) {
                            io.heat(false);
                        } else {
                            io.heat(true);
                        }

                        //Do we really need this?
                        if (io.getTemperature() < value - 2 + 0.00952) {
                            io.heat(true);
                        }
                }
            }
        } catch (InterruptedException unexpected) {
            throw new Error(unexpected);
        }
    }
}
