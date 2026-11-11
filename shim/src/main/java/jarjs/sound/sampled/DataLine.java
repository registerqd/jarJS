package jarjs.sound.sampled;

public interface DataLine extends Line {
    int available();
    int getBufferSize();
    AudioFormat getFormat();
    int getFramePosition();
    float getLevel();
    long getLongFramePosition();
    long getMicrosecondPosition();

    class Info extends Line.Info {
        private final AudioFormat[] formats;
        public Info(Class<?> lineClass, AudioFormat format) {
            super(lineClass);
            this.formats = new AudioFormat[]{format};
        }
        public Info(Class<?> lineClass, AudioFormat[] formats) {
            super(lineClass);
            this.formats = formats;
        }
        public AudioFormat[] getFormats() { return formats; }
        public boolean isFormatSupported(AudioFormat format) { return true; }
    }
}
