package jarjs.sound.midi;

/** javax.sound.midi.ShortMessage 兼容层。 */
public class ShortMessage {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final int PROGRAM_CHANGE = 0xC0;
    public static final int CONTROL_CHANGE = 0xB0;

    private int command = 0;
    private int data1 = 0;
    private int data2 = 0;

    public void setMessage(int command) { this.command = command; }
    public void setMessage(int command, int data1, int data2) {
        this.command = command; this.data1 = data1; this.data2 = data2;
    }

    public int getCommand() { return command; }
    public int getData1() { return data1; }
    public int getData2() { return data2; }
    public int getLength() { return 3; }
    public int getStatus() { return command; }
}
