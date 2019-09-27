import lift.LiftView;
import lift.Passenger;

import java.util.Arrays;
import java.util.Random;

public class Lift {

    private int here;
    private int[] waitEntry = new int[7];
    private int[] waitExit = new int[7];
    private int load;
    private boolean liftHasStopped = false;

    private void createPassengers(LiftView liftView) {
        int passengerAmount = 20;
        Thread[] passengerThreads = new Thread[passengerAmount];
        for(int i = 0; i < passengerAmount; i++){
            passengerThreads[i] = new Thread( () -> {
                try {
                    passengerThreadThings(liftView);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, ("passenger_thread_" + i));
            passengerThreads[i].start();
        }
    }

    private void passengerThreadThings(LiftView liftView) throws InterruptedException {
        Passenger passenger = liftView.createPassenger();
        Random rand = new Random();
        Thread.sleep(1000 * rand.nextInt(45));

        passenger.begin();
        passengerThings(passenger);
        passenger.end();
    }

    private synchronized void passengerThings(Passenger passenger) throws InterruptedException {
        waitEntry[passenger.getStartFloor()] += 1;
        notifyAll();

        while (!(here == passenger.getStartFloor() && load < 4 && liftHasStopped)) {
            wait();
        }

        load += 1;
        passenger.enterLift();
        waitEntry[passenger.getStartFloor()] -= 1;
        waitExit[passenger.getDestinationFloor()] += 1;
        notifyAll();

        while (!(here == passenger.getDestinationFloor() && liftHasStopped)) {
            wait();
        }

        passenger.exitLift();
        waitExit[here]--;
        load--;
        notifyAll();
    }

    private int nextLevel(int here, boolean directionIsUp){
        if(directionIsUp){
            return here + 1;
        } else {
            return here - 1;
        }
    }

    private void liftThreadThings(LiftView liftView) {
        here = 0;
        boolean directionIsUp = false;

        while(true){
            if(here == 6 || here == 0) {
                directionIsUp = !directionIsUp;
            }

            liftThings();

            int next = nextLevel(here, directionIsUp);
            liftView.moveLift(here, next);
            here = next;
        }
    }

    private synchronized void liftThings() {

        while (liftShouldStop(here)) {
            liftHasStopped = true;
            notifyAll();
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        liftHasStopped = false;
        notifyAll();
    }

    private boolean liftShouldStop(int here) {
        Boolean noPassengers = Arrays.stream(waitEntry).sum() == 0 && Arrays.stream(waitExit).sum() == 0;

        return (waitEntry[here] > 0 && load < 4) || waitExit[here] > 0 || noPassengers;
    }

    public static void main(String[] args) {
        LiftView liftView = new LiftView();
        Lift lift = new Lift();

        lift.createPassengers(liftView);

        Thread liftThread = new Thread(() -> lift.liftThreadThings(liftView), "Lift Thread");
        liftThread.start();
    }
}
