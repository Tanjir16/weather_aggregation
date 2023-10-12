public class LamportClock {
    private int time;

    public LamportClock() {
        time = 0;
    }

    // Call this method when a send or receive action occurs
    public synchronized void sendAction() {
        time++;
    }

    // Call this method when a receive action occurs and pass the received timestamp
    public synchronized void receiveAction(int receivedTimestamp) {
        time = Math.max(time, receivedTimestamp) + 1;
    }

    // Get the current Lamport clock time
    public synchronized int getTime() {
        return time;
    }
}