package jarjs.net;

import org.teavm.jso.JSBody;
import java.nio.ByteBuffer;

/**
 * 浏览器 WebSocket 二进制网络桥。
 * 用于替代系统原生 SocketChannel / ServerSocketChannel，打通网络通信。
 */
public final class WebSocketBridge {

    private WebSocketBridge() {}

    @JSBody(params = {"url"}, script =
            "var ws = new WebSocket(url);\n" +
            "ws.binaryType = 'arraybuffer';\n" +
            "window.__jarjs_ws = ws;\n" +
            "ws.onopen = function() { console.log('[JARJS Net] WebSocket 已连接: ' + url); };\n" +
            "ws.onerror = function(e) { console.warn('[JARJS Net] WebSocket 错误', e); };\n" +
            "return 1;")
    public static native int connect(String url);

    @JSBody(params = {"data"}, script =
            "if (window.__jarjs_ws && window.__jarjs_ws.readyState === 1) {\n" +
            "  var bin = atob(data);\n" +
            "  var buf = new Uint8Array(bin.length);\n" +
            "  for (var i = 0; i < bin.length; i++) buf[i] = bin.charCodeAt(i);\n" +
            "  window.__jarjs_ws.send(buf.buffer);\n" +
            "}")
    public static native void sendBase64(String data);

    public static void send(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return;
        sendBase64(base64(bytes));
    }

    public static void send(ByteBuffer buf) {
        if (buf == null) return;
        int n = buf.remaining();
        byte[] bytes = new byte[n];
        int pos = buf.position();
        for (int i = 0; i < n; i++) bytes[i] = buf.get(pos + i);
        send(bytes);
    }

    @JSBody(params = {}, script =
            "return (window.__jarjs_ws && window.__jarjs_ws.readyState === 1) ? 1 : 0;")
    public static native int isConnected();

    @JSBody(params = {}, script =
            "if (window.__jarjs_ws) { window.__jarjs_ws.close(); window.__jarjs_ws = null; }")
    public static native void close();

    private static String base64(byte[] data) {
        final char[] T = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        StringBuilder sb = new StringBuilder((data.length + 2) / 3 * 4);
        int i = 0;
        while (i + 2 < data.length) {
            int n = ((data[i] & 0xFF) << 16) | ((data[i + 1] & 0xFF) << 8) | (data[i + 2] & 0xFF);
            sb.append(T[(n >> 18) & 63]).append(T[(n >> 12) & 63])
              .append(T[(n >> 6) & 63]).append(T[n & 63]);
            i += 3;
        }
        int rem = data.length - i;
        if (rem == 1) {
            int n = (data[i] & 0xFF) << 16;
            sb.append(T[(n >> 18) & 63]).append(T[(n >> 12) & 63]).append("==");
        } else if (rem == 2) {
            int n = ((data[i] & 0xFF) << 16) | ((data[i + 1] & 0xFF) << 8);
            sb.append(T[(n >> 18) & 63]).append(T[(n >> 12) & 63])
              .append(T[(n >> 6) & 63]).append('=');
        }
        return sb.toString();
    }
}
