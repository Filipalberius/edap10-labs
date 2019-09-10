package clock;

public class Time {
    private int currentTime;
    private ClockOutput out;

    public Time(int startTime, ClockOutput out){
        currentTime = startTime;
        this.out = out;
    }

    public void tick() {
        while (true) {
            currentTime += 1;
            out.displayTime(currentTime);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }
}
