import java.util.concurrent.Semaphore;

import clock.ClockInput;
import clock.ClockInput.UserInput;
import clock.ClockOutput;
import clock.Clock;
import emulator.AlarmClockEmulator;

public class ClockMain {

    public static void main(String[] args) throws InterruptedException {
        AlarmClockEmulator emulator = new AlarmClockEmulator();

        ClockInput  in  = emulator.getInput();
        ClockOutput out = emulator.getOutput();
        Semaphore sem = in.getSemaphore();

        Semaphore alarmSemaphore = new Semaphore(0);
        Semaphore timeMutex = new Semaphore(1);

        Clock clock = new Clock(0, out, timeMutex);

        Thread clockThread = new Thread( () -> clock.tick(alarmSemaphore), "clock");
        Thread alarmThread = new Thread( () -> clock.soundTheAlarm(alarmSemaphore), "alarm");

        clockThread.start();  //startar tråden som tickar klockan och kör displayTime.
        alarmThread.start();  //startar tråden som kollar larmet.

        while (true) {
            sem.acquire();  // wait for user input

            UserInput userInput = in.getUserInput();
            int choice = userInput.getChoice();
            int value = userInput.getValue();

            switch (choice) {
                case 1:
                    clock.setCurrentTime(value);
                    break;
                case 2:
                    clock.setAlarmTime(value);
                    break;
                case 3:
                    clock.toggleAlarm();
                    break;
                case 4:
                    alarmSemaphore.drainPermits();
                    break;
            }

            System.out.println("choice = " + choice + "  value=" + value);
        }
    }
}
