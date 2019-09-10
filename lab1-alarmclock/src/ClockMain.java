import java.util.concurrent.Semaphore;

import clock.ClockInput;
import clock.ClockInput.UserInput;
import clock.ClockOutput;
import clock.TimeCounter;
import emulator.AlarmClockEmulator;

public class ClockMain {

    public static void main(String[] args) throws InterruptedException {
        AlarmClockEmulator emulator = new AlarmClockEmulator();

        ClockInput  in  = emulator.getInput();
        ClockOutput out = emulator.getOutput();

        out.displayTime(133700);   // arbitrary time: just an example

        Semaphore sem = in.getSemaphore();

        TimeCounter timeCounter = new TimeCounter(112233, out);
        Thread clockCounter = new Thread(timeCounter::tick);
        clockCounter.start();

        while (true) {
            sem.acquire();                        // wait for user input

            UserInput userInput = in.getUserInput();
            int choice = userInput.getChoice();
            int value = userInput.getValue();

            sem.release();

            System.out.println("choice = " + choice + "  value=" + value);
        }
    }
}
