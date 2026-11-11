package jarjs.assets;

import jarjs.media.MediaBackend;

/**
 * 资源访问层：构建时 jar 内的所有资源都被转成 base64 内嵌进 HTML，
 * 运行时通过这张表读取，不发起任何网络请求。
 */
public final class Assets {

    private Assets() {}

    public static boolean exists(String path) {
        return MediaBackend.assetExists(normalize(path));
    }

    /** 资源原始字节（从 base64 还原）。 */
    public static byte[] getBytes(String path) {
        String b64 = MediaBackend.assetBase64(normalize(path));
        if (b64 == null) return null;
        String bin = MediaBackend.atob(b64);
        byte[] out = new byte[bin.length()];
        for (int i = 0; i < bin.length(); i++) out[i] = (byte) bin.charAt(i);
        return out;
    }

    /** 文本资源（UTF-8）。 */
    public static String getText(String path) {
        byte[] b = getBytes(path);
        if (b == null) return null;
        try {
            return new String(b, "UTF-8");
        } catch (Exception e) {
            return new String(b);
        }
    }

    /** 直接拿 data URL（给 Image / Audio 用）。 */
    public static String getDataUrl(String path) {
        return MediaBackend.assetDataUrl(normalize(path));
    }

    public static int size(String path) {
        return MediaBackend.assetSize(normalize(path));
    }

    public static String mime(String path) {
        return MediaBackend.assetMime(normalize(path));
    }

    public static String[] list() {
        String[] a = MediaBackend.assetList();
        return a == null ? new String[0] : a;
    }

    /** 兼容 Class.getResource 的写法："/assets/x.png" → "assets/x.png"。 */
    private static String normalize(String path) {
        if (path == null) return "";
        String p = path;
        while (p.startsWith("/")) p = p.substring(1);
        return p;
    }
}
