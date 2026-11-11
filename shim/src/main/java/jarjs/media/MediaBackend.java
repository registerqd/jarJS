package jarjs.media;

import org.teavm.jso.JSBody;

/**
 * 多媒体后端桥（v0.4）：OpenAL 音频、图像解码、字体光栅化、base64 资源表。
 * JS 侧实现位于 runtime/jarjs-media.js，构建时自动内联。
 */
public final class MediaBackend {

    private MediaBackend() {}

    // ---------- 音频 ----------
    @JSBody(params = {"id", "b64", "format", "freq"},
            script = "window.JARJS_MEDIA.alBufferData(id, b64, format, freq);")
    public static native void alBufferData(int id, String b64, int format, int freq);

    @JSBody(params = {"id", "dataUrl"}, script = "window.JARJS_MEDIA.alBufferDataEncoded(id, dataUrl);")
    public static native void alBufferDataEncoded(int id, String dataUrl);

    @JSBody(params = {"n"}, script = "return window.JARJS_MEDIA.alGenBuffers(n);")
    public static native int[] alGenBuffers(int n);

    @JSBody(params = {"n"}, script = "return window.JARJS_MEDIA.alGenSources(n);")
    public static native int[] alGenSources(int n);

    @JSBody(params = {"id", "pname", "value"}, script = "window.JARJS_MEDIA.alSourcei(id, pname, value);")
    public static native void alSourcei(int id, int pname, int value);

    @JSBody(params = {"id", "pname", "value"}, script = "window.JARJS_MEDIA.alSourcef(id, pname, value);")
    public static native void alSourcef(int id, int pname, float value);

    @JSBody(params = {"id", "pname", "x", "y", "z"}, script = "window.JARJS_MEDIA.alSource3f(id, pname, x, y, z);")
    public static native void alSource3f(int id, int pname, float x, float y, float z);

    @JSBody(params = {"id"}, script = "window.JARJS_MEDIA.alSourcePlay(id);")
    public static native void alSourcePlay(int id);

    @JSBody(params = {"ids"}, script = "window.JARJS_MEDIA.alSourcePlayv(ids);")
    public static native void alSourcePlayv(int[] ids);

    @JSBody(params = {"id"}, script = "window.JARJS_MEDIA.alSourceStop(id);")
    public static native void alSourceStop(int id);

    @JSBody(params = {"id"}, script = "window.JARJS_MEDIA.alSourcePause(id);")
    public static native void alSourcePause(int id);

    @JSBody(params = {"id"}, script = "window.JARJS_MEDIA.alSourceRewind(id);")
    public static native void alSourceRewind(int id);

    @JSBody(params = {"ids"}, script = "window.JARJS_MEDIA.alDeleteBuffers(ids);")
    public static native void alDeleteBuffers(int[] ids);

    @JSBody(params = {"ids"}, script = "window.JARJS_MEDIA.alDeleteSources(ids);")
    public static native void alDeleteSources(int[] ids);

    @JSBody(params = {"pname", "value"}, script = "window.JARJS_MEDIA.alListenerf(pname, value);")
    public static native void alListenerf(int pname, float value);

    @JSBody(params = {"pname", "x", "y", "z"}, script = "window.JARJS_MEDIA.alListener3f(pname, x, y, z);")
    public static native void alListener3f(int pname, float x, float y, float z);

    @JSBody(params = {}, script = "return window.JARJS_MEDIA.alGetError();")
    public static native int alGetError();

    @JSBody(params = {"id"}, script = "return window.JARJS_MEDIA.alIsSourcePlaying(id);")
    public static native int alIsSourcePlaying(int id);

    @JSBody(params = {}, script = "return window.JARJS_MEDIA.audioState();")
    public static native String audioState();

    // ---------- 图像 ----------
    @JSBody(params = {"key", "dataUrl"}, script = "window.JARJS_MEDIA.loadImage(key, dataUrl, null);")
    public static native void loadImage(String key, String dataUrl);

    @JSBody(params = {"key"}, script = "return window.JARJS_MEDIA.isImageReady(key);")
    public static native int isImageReady(String key);

    @JSBody(params = {"key"}, script = "return window.JARJS_MEDIA.getImageWidth(key);")
    public static native int getImageWidth(String key);

    @JSBody(params = {"key"}, script = "return window.JARJS_MEDIA.getImageHeight(key);")
    public static native int getImageHeight(String key);

    @JSBody(params = {"key"}, script = "return window.JARJS_MEDIA.getImagePixels(key);")
    public static native int[] getImagePixels(String key);

    // ---------- 字体 ----------
    @JSBody(params = {"text", "family", "size", "bold", "italic"},
            script = "var r = window.JARJS_MEDIA.rasterizeText(text, family, size, bold, italic);"
                   + "return [r.width, r.height, r.advance, r.baseline];")
    public static native int[] textMetrics(String text, String family, float size, boolean bold, boolean italic);

    @JSBody(params = {"text", "family", "size", "bold", "italic"},
            script = "return window.JARJS_MEDIA.rasterizeText(text, family, size, bold, italic).pixels;")
    public static native int[] textPixels(String text, String family, float size, boolean bold, boolean italic);

    @JSBody(params = {"text", "family", "size", "bold", "italic"},
            script = "return window.JARJS_MEDIA.measureText(text, family, size, bold, italic);")
    public static native int measureText(String text, String family, float size, boolean bold, boolean italic);

    // ---------- 资源表 ----------
    @JSBody(params = {"path"}, script = "return window.JARJS_MEDIA.assetExists(path);")
    public static native boolean assetExists(String path);

    @JSBody(params = {"path"}, script = "return window.JARJS_MEDIA.assetDataUrl(path);")
    public static native String assetDataUrl(String path);

    @JSBody(params = {"path"}, script = "return window.JARJS_MEDIA.assetBase64(path);")
    public static native String assetBase64(String path);

    @JSBody(params = {"path"}, script = "return window.JARJS_MEDIA.assetSize(path);")
    public static native int assetSize(String path);

    @JSBody(params = {"path"}, script = "return window.JARJS_MEDIA.assetMime(path);")
    public static native String assetMime(String path);

    @JSBody(params = {}, script = "return window.JARJS_MEDIA.assetList();")
    public static native String[] assetList();

    @JSBody(params = {"b64"}, script = "return atob(b64);")
    public static native String atob(String b64);

    @JSBody(params = {}, script = "return JSON.stringify(window.JARJS_MEDIA.debugStats());")
    public static native String debugStats();

    @JSBody(params = {"msg"}, script = "if (window.console) console.warn('[JARJS Media] ' + msg);")
    public static native void warn(String msg);
}
