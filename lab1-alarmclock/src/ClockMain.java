import java.util.concurrent.Semaphore;

import clock.ClockInput;
import clock.ClockInput.UserInput;
import clock.ClockOutput;
import clock.Time;
import emulator.AlarmClockEmulator;

public class ClockMain {

    public static void main(String[] args) throws InterruptedException {
        AlarmClockEmulator emulator = new AlarmClockEmulator();

        ClockInput  in  = emulator.getInput();
        ClockOutput out = emulator.getOutput();
        Semaphore sem = in.getSemaphore();

        Time time = new Time(0, out);
        Thread clock = new Thread(time::tick);
        clock.start();  //startar tråden som tickar klockan och kör displayTime

        while (true) {
            sem.acquire();  // wait for user input

            UserInput userInput = in.getUserInput();
            int choice = userInput.getChoice();
            int value = userInput.getValue();

            System.out.println("choice = " + choice + "  value=" + value);
        }
    }
}
