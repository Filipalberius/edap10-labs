package lab;

import wash.WashingIO;

public class SpinController extends MessagingThread<WashingMessage> {

    private WashingIO io;

    SpinController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {
        try {
            boolean spinLeft = true;
            int command = 0;

            while (true) {
                // wait for up to a (simulated) minute for a WashingMessage
                WashingMessage m = receiveWithTimeout(60000 / Wash.SPEEDUP);

                // if m is null, it means a minute passed and no message was received
                if (m != null) {
                    System.out.println("got " + m);
                    command = m.getCommand();
                }

                switch (command) {
                    case 2:
                        spinLeft = !spinLeft;
                        if (spinLeft) {
                            io.setSpinMode(2);
                        } else {
                            io.setSpinMode(3);
                        }
                        break;
                    case 3:
                        io.setSpinMode(4);
                        break;
                    case 1:
                        io.setSpinMode(1);
                        break;
                }

            }
        } catch (InterruptedException unexpected) {
            throw new Error(unexpected);
        }
    }
}
