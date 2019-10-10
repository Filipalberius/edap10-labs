package lab;

import wash.WashingIO;

class WashingProgram2 extends MessagingThread<WashingMessage> {

    private WashingIO io;
    private MessagingThread<WashingMessage> temp;
    private MessagingThread<WashingMessage> water;
    private MessagingThread<WashingMessage> spin;

    public WashingProgram2(WashingIO io,
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

            // Switch off heating
            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));

            // Switch off spin
            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));

            // Drain barrel (may take some time)
            water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
            WashingMessage ack = receive();  // wait for acknowledgment
            System.out.println("got " + ack);
            water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));

            // Unlock hatch
            io.lock(false);
            
        } catch (InterruptedException e) {
            
            // if we end up here, it means the program was interrupt()'ed
            // set all controllers to idle

            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
            water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));
            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
            System.out.println("washing program terminated");
        }
    }
}
