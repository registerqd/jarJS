package jarjs.sound.midi;

/** javax.sound.midi.MidiDevice 兼容层。 */
public class MidiDevice {
    public void open() {}
    public void close() {}
    public boolean isOpen() { return false; }
    public Info getDeviceInfo() { return new Info(); }
    public int getMaxReceivers() { return 0; }
    public int getMaxTransmitters() { return 0; }

    public static class Info {
        public String getName() { return "jarJS MIDI"; }
        public String getVendor() { return "jarJS"; }
        public String getDescription() { return "Web Audio MIDI bridge"; }
        public String getVersion() { return "1.0"; }
    }
}
