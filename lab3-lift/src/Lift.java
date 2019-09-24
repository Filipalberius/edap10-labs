import lift.LiftView;
import lift.Passenger;

import java.util.Random;

public class Lift {

    private int here; // If here!=next, here (floor number) tells from which floor the lift is moving and next to which floor it is moving.
    private int next; // If here==next, the lift is standing still on the floor given by here.
    private int[] waitEntry; // Number of passengers waiting to enter the lift at the various floors.
    private int[] waitExit; // Number of passengers (inside the lift) waiting to leave the lift at the various floors.
    private int load; // Number of passengers currently in the lift.
    private int passengerAmmount;
    private static Thread[] passengerThreads;

    public synchronized int getHere() {
        return here;
    }

    public synchronized void setHere(int newHere) {
        this.here = newHere;
    }

    public synchronized int getNext() {
        return next;
    }

    public synchronized void setNext(int newNext) {
        this.next = newNext;
    }

    public synchronized int[] getWaitEntry() {
        return waitEntry;
    }

    public synchronized void setWaitEntry(int[] newWaitEntry) {
        this.waitEntry = newWaitEntry;
    }

    public synchronized int[] getWaitExit() {
        return waitExit;
    }

    public synchronized void setWaitExit(int[] newWaitExit) {
        this.waitExit = newWaitExit;
    }

    public synchronized int getLoad() {
        return load;
    }

    public synchronized void setLoad(int newLoad) {
        this.load = newLoad;
    }

    private synchronized void waitOutside(long millis) throws InterruptedException {
        long timeToWakeUp = System.currentTimeMillis() + millis;

        while (System.currentTimeMillis() < timeToWakeUp) {
            long dt = timeToWakeUp - System.currentTimeMillis();
            wait(dt);
        }
    }

    private synchronized void createPassengers(LiftView liftView, int passengerAmmount) {
        passengerThreads = new Thread[passengerAmmount];
        for(int i = 0; i < passengerAmmount; i++){
            passengerThreads[i] = new Thread( () -> {
                try {
                    doPassengerThreadThings(liftView);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            passengerThreads[i].start();
        }
    }

    private void doPassengerThreadThings(LiftView liftView) throws InterruptedException {
        System.out.println("Thread started");
        Passenger passenger = liftView.createPassenger();

        Random rand = new Random();
        long delay = 1000 * rand.nextInt(45);

        waitOutside(delay);

        passenger.begin();
    }

    public static void main(String[] args) {
        LiftView liftView = new LiftView();
        Lift lift = new Lift();
        lift.createPassengers(liftView, 20);
    }
}
