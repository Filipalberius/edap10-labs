package lab;

import wash.WashingIO;

public class WaterController extends MessagingThread<WashingMessage> {

    private WashingIO io;

    WaterController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {
        try {
            int command = 0;
            double value = 0;
            MessagingThread<WashingMessage> sender = null;

            while (true) {
                WashingMessage m = receiveWithTimeout(1000 / Wash.SPEEDUP);

                if (m != null) {
                    System.out.println("got " + m);
                    command = m.getCommand();
                    value = m.getValue();
                    sender = m.getSender();
                }

                switch (command) {
                    case 6:
                        //IDLE
                        io.fill(false);
                        io.drain(false);
                        break;
                    case 7:
                        //FILL (to value)
                        if (io.getWaterLevel() < value) {
                            io.fill(true);
                        }
                        else {
                            io.fill(false);
                            sender.send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
                        }
                        break;
                    case 8:
                        //DRAIN
                        if (io.getWaterLevel() > 0)
                            io.drain(true);
                        else
                            io.drain(false);
                        break;
                }

            }

        } catch (InterruptedException unexpected) {
            throw new Error(unexpected);
        }
    }
}
