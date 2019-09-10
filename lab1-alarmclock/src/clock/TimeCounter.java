package clock;

public class TimeCounter {
    private int currentTime;
    private ClockOutput output;

    public TimeCounter(int startTime, ClockOutput out){
        currentTime = startTime;
        output = out;
    }

    public void tick() {
        while (true) {
            currentTime += 1;
            output.displayTime(currentTime);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }
}
