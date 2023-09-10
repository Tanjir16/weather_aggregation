public class LamportClock {
    private int timestamp;

    public LamportClock() {
        this.timestamp = 0;
    }

    public void tick() {
        timestamp++;
    }

    public void sendAction() {
        tick(); // Increment before sending an event
    }

    public void receiveAction(int sourceTimestamp) {
        timestamp = Math.max(timestamp, sourceTimestamp) + 1;
    }

    public int getTime() {
        return timestamp;
    }
}
