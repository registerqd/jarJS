package jarjs.sound.midi;

public class Sequencer {
    private Sequence sequence;
    private boolean open = false;

    public void setSequence(Sequence sequence) { this.sequence = sequence; }
    public void open() { this.open = true; }
    public void close() { this.open = false; }
    public boolean isOpen() { return open; }
    public void start() {}
    public void stop() {}
    public boolean isRunning() { return false; }
    public void setLoopCount(int count) {}
}
