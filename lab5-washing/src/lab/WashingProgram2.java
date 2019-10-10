package lab;

import wash.WashingIO;

class WashingProgram2 extends MessagingThread<WashingMessage> {

    private WashingIO io;
    private MessagingThread<WashingMessage> temp;
    private MessagingThread<WashingMessage> water;
    private MessagingThread<WashingMessage> spin;

    WashingProgram2(WashingIO io,
                           MessagingThread<WashingMessage> temp,
                           MessagingThread<WashingMessage> water,
                           MessagingThread<WashingMessage> spin) {
        this.io = io;
        this.temp = temp;
        this.water = water;
        this.spin = spin;
    }
    
    @Override
    public void run() {
        try {
            WashingMessage ack;

            io.lock(true);

            water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10));
            ack = receive();
            System.out.println("got " + ack);

            temp.send(new WashingMessage(this, WashingMessage.TEMP_SET, 40));
            ack = receive();
            System.out.println("got " + ack);

            spin.send(new WashingMessage(this, WashingMessage.SPIN_SLOW));

            Thread.sleep(900000 / Wash.SPEEDUP);

            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));

            water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
            ack = receive();
            System.out.println("got " + ack);

            water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10));
            ack = receive();
            System.out.println("got " + ack);

            temp.send(new WashingMessage(this, WashingMessage.TEMP_SET, 60));
            ack = receive();
            System.out.println("got " + ack);

            spin.send(new WashingMessage(this, WashingMessage.SPIN_SLOW));

            Thread.sleep(1800000 / Wash.SPEEDUP);

            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));

            water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
            ack = receive();
            System.out.println("got " + ack);

            for (int i = 0; i < 5; i++) {
                water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10));
                ack = receive();
                System.out.println("got " + ack);

                spin.send(new WashingMessage(this, WashingMessage.SPIN_SLOW));

                Thread.sleep(120000 / Wash.SPEEDUP);

                spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));

                water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
                ack = receive();
                System.out.println("got " + ack);

            }

            spin.send(new WashingMessage(this, WashingMessage.SPIN_FAST));

            Thread.sleep(300000 / Wash.SPEEDUP);

            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));

            io.lock(false);
            
        } catch (InterruptedException e) {
            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
            water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));
            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
        }
    }
}
