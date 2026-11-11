package jarjs.sound.midi;

/** javax.sound.midi.MidiChannel 兼容层。 */
public class MidiChannel {
    public void noteOn(int noteNumber, int velocity) {}
    public void noteOff(int noteNumber) {}
    public void noteOff(int noteNumber, int velocity) {}
    public void programChange(int program) {}
    public void programChange(int bank, int program) {}
    public void controlChange(int controller, int value) {}
    public void setMute(boolean mute) {}
    public boolean getMute() { return false; }
    public void setSolo(boolean solo) {}
    public boolean getSolo() { return false; }
    public void setPan(int pan) {}
    public void setVolume(int volume) {}
    public void allNotesOff() {}
    public void allSoundOff() {}
    public void resetAllControllers() {}
}
