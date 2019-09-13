package clock;

import java.util.concurrent.Semaphore;

public class Clock {
    private int currentTime;
    private ClockOutput out;
    private int alarmTime = 0;
    private boolean alarmIsActive;
    private Semaphore mutex;
    private int alarmCounter;

    public Clock(int startTime, ClockOutput out, Semaphore mutex){
        currentTime = startTime;
        this.out = out;
        this.mutex = mutex;
    }

    public void setCurrentTime(int time) {
        try {
            mutex.acquire();
            currentTime = time;
            mutex.release();
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    public void setAlarmTime(int time) {
        alarmTime = time;
    }

    public void tick() throws InterruptedException {

        long t0 = System.currentTimeMillis();
        long n = 0;

        while (true) {
            n += 1;

            mutex.acquire();

            out.displayTime(currentTime);
            currentTime = nextTime(currentTime + 1);

            if (currentTime == alarmTime + 1 && alarmIsActive) {
                alarmCounter = 21;
            }

            if (alarmCounter > 0) {
                alarmCounter--;
                out.alarm();
            }

            mutex.release();

            long targetTime = t0 + (n * 1000);
            long sleep = targetTime - System.currentTimeMillis();

            Thread.sleep(sleep);
        }
    }

    public void stopAlarm() {
        alarmCounter = 0;
    }


    private int nextTime(int time) {
        int sec = time % 100;
        int min = (time / 100) % 100;
        int hr = (time / 10000) % 100;

        if (sec >= 60) {
            sec = 0;
            min += 1;
        }

        if (min >= 60) {
            min = 0;
            hr += 1;
        }

        if (hr >= 24) {
            hr = 0;
            min = 0;
            sec = 0;
        }

        return (hr * 10000) + (min * 100) + sec;
    }

    public void toggleAlarm(){
        alarmIsActive = !alarmIsActive;
        out.setAlarmIndicator(alarmIsActive);
    }
}
