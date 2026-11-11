package jarjs.sound.midi;

import java.io.InputStream;

/**
 * javax.sound.midi.MidiSystem / Sequencer / Sequence 兼容层。
 * 桌面游戏在播放背景音乐时可能会调用，提供防崩空桩与状态模拟。
 */
public final class MidiSystem {

    private MidiSystem() {}

    public static Sequence getSequence(InputStream stream) {
        return new Sequence();
    }

    public static Sequencer getSequencer() {
        return new Sequencer();
    }
}
