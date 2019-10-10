package lab;
import simulator.WashingSimulator;
import wash.WashingIO;

public class Wash {

    // simulation speed-up factor:
    // 50 means the simulation is 50 times faster than real time
    static final int SPEEDUP = 50;

    public static void main(String[] args) throws InterruptedException {
        WashingSimulator sim = new WashingSimulator(SPEEDUP);
        
        WashingIO io = sim.startSimulation();

        TemperatureController temp = new TemperatureController(io);
        WaterController water = new WaterController(io);
        SpinController spin = new SpinController(io);

        temp.start();
        water.start();
        spin.start();

        MessagingThread<WashingMessage> prog1 = new WashingProgram1(io, temp, water, spin);
        MessagingThread<WashingMessage> prog2 = new WashingProgram2(io, temp, water, spin);
        MessagingThread<WashingMessage> prog3 = new WashingProgram3(io, temp, water, spin);

        int currentProgramme = 0;

        while (true) {
            int userInput = io.awaitButton();

            System.out.println("user selected program " + userInput);

            switch (userInput) {
                case 0:
                    switch (currentProgramme) {
                        case 1:
                            prog1.interrupt();
                            prog1 = new WashingProgram1(io, temp, water, spin);
                            break;
                        case 2:
                            prog2.interrupt();
                            prog2 = new WashingProgram2(io, temp, water, spin);
                            break;
                        case 3:
                            prog3.interrupt();
                            prog3 = new WashingProgram3(io, temp, water, spin);
                            break;
                    }
                    break;

                case 1:
                    prog1.start();
                    currentProgramme = 1;
                    break;

                case 2:
                    prog2.start();
                    currentProgramme = 2;
                    break;

                case 3:
                    prog3.start();
                    currentProgramme = 3;
                    break;
            }
        }
    }
}
