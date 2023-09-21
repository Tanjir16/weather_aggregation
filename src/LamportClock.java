public class LamportClock {
    private int timestamp;

    public LamportClock() {
        this.timestamp = 0;
    }

    public synchronized int getTime() {
        return this.timestamp;
    }

    public synchronized void sendAction() {
        this.timestamp += 1;
    }

    public synchronized void receiveAction(int sourceTimestamp) {
        this.timestamp = Math.max(this.timestamp, sourceTimestamp) + 1;
    }
}
