package jarjs.sound.midi;

/** javax.sound.midi.Synthesizer 兼容层。 */
public class Synthesizer extends MidiDevice {
    public MidiChannel[] getChannels() { return new MidiChannel[16]; }
    public boolean isSoundbankSupported(Object soundbank) { return false; }
    public void loadAllInstruments(Object soundbank) {}
    public void unloadAllInstruments(Object soundbank) {}
}
