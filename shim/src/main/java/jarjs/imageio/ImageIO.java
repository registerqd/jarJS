package jarjs.imageio;

import jarjs.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.Inflater;

/**
 * javax.imageio.ImageIO 兼容层。
 * 内置纯 Java 零依赖 PNG 解码器（基于 ZLIB Inflater），支持游戏同步读取贴图。
 */
public final class ImageIO {

    private ImageIO() {}

    public static BufferedImage read(File input) {
        if (input == null || !input.exists()) return null;
        try (InputStream in = new FileInputStream(input)) {
            return read(in);
        } catch (Exception e) {
            return null;
        }
    }

    public static BufferedImage read(URL input) {
        if (input == null) return null;
        try (InputStream in = input.openStream()) {
            return read(in);
        } catch (Exception e) {
            return null;
        }
    }

    public static BufferedImage read(InputStream input) {
        if (input == null) return null;
        try {
            byte[] bytes = readAllBytes(input);
            return decodePNG(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] readAllBytes(InputStream in) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    /**
     * 纯 Java PNG 解码器：解析 IHDR 与 IDAT 数据流，支持 8-bit RGBA / RGB / 调色板格式。
     */
    private static BufferedImage decodePNG(byte[] data) {
        if (data == null || data.length < 24) return null;
        // 校验 PNG 魔数：89 50 4E 47 0D 0A 1A 0A
        if ((data[0] & 0xFF) != 0x89 || data[1] != 'P' || data[2] != 'N' || data[3] != 'G') {
            return null;
        }

        int width = 0, height = 0, bitDepth = 0, colorType = 0;
        ByteArrayOutputStream idatStream = new ByteArrayOutputStream();
        byte[] palette = null;

        int pos = 8;
        while (pos + 8 <= data.length) {
            int len = readInt(data, pos);
            String type = new String(data, pos + 4, 4);
            int chunkData = pos + 8;

            if ("IHDR".equals(type) && len >= 13) {
                width = readInt(data, chunkData);
                height = readInt(data, chunkData + 4);
                bitDepth = data[chunkData + 8] & 0xFF;
                colorType = data[chunkData + 9] & 0xFF;
            } else if ("PLTE".equals(type)) {
                palette = new byte[len];
                System.arraycopy(data, chunkData, palette, 0, len);
            } else if ("IDAT".equals(type)) {
                idatStream.write(data, chunkData, len);
            } else if ("IEND".equals(type)) {
                break;
            }
            pos += 12 + len;
        }

        if (width <= 0 || height <= 0) return null;

        try {
            // ZLIB 解压 IDAT 压缩数据流
            byte[] compressed = idatStream.toByteArray();
            Inflater inflater = new Inflater();
            inflater.setInput(compressed);

            int bpp;
            if (colorType == 6) bpp = 4;      // RGBA
            else if (colorType == 2) bpp = 3; // RGB
            else if (colorType == 3) bpp = 1; // 调色板
            else if (colorType == 0) bpp = 1; // 灰度
            else bpp = 4;

            int scanline = 1 + width * bpp;
            byte[] raw = new byte[scanline * height];
            int decompressed = inflater.inflate(raw);
            inflater.end();

            if (decompressed < scanline * height) {
                // 部分解码容错
            }

            int[] pixels = new int[width * height];
            byte[] prevLine = new byte[width * bpp];
            byte[] currLine = new byte[width * bpp];

            for (int y = 0; y < height; y++) {
                int lineOff = y * scanline;
                int filter = raw[lineOff] & 0xFF;

                for (int x = 0; x < width * bpp; x++) {
                    int val = raw[lineOff + 1 + x] & 0xFF;
                    int a = (x >= bpp) ? (currLine[x - bpp] & 0xFF) : 0;
                    int b = prevLine[x] & 0xFF;
                    int c = (x >= bpp) ? (prevLine[x - bpp] & 0xFF) : 0;

                    if (filter == 1) val = (val + a) & 0xFF;          // Sub
                    else if (filter == 2) val = (val + b) & 0xFF;     // Up
                    else if (filter == 3) val = (val + ((a + b) / 2)) & 0xFF; // Average
                    else if (filter == 4) val = (val + paeth(a, b, c)) & 0xFF; // Paeth

                    currLine[x] = (byte) val;
                }

                // 像素打包
                for (int x = 0; x < width; x++) {
                    int r = 255, g = 255, b = 255, a = 255;
                    if (colorType == 6) { // RGBA
                        r = currLine[x * 4] & 0xFF;
                        g = currLine[x * 4 + 1] & 0xFF;
                        b = currLine[x * 4 + 2] & 0xFF;
                        a = currLine[x * 4 + 3] & 0xFF;
                    } else if (colorType == 2) { // RGB
                        r = currLine[x * 3] & 0xFF;
                        g = currLine[x * 3 + 1] & 0xFF;
                        b = currLine[x * 3 + 2] & 0xFF;
                    } else if (colorType == 3 && palette != null) { // 调色板
                        int idx = currLine[x] & 0xFF;
                        if (idx * 3 + 2 < palette.length) {
                            r = palette[idx * 3] & 0xFF;
                            g = palette[idx * 3 + 1] & 0xFF;
                            b = palette[idx * 3 + 2] & 0xFF;
                        }
                    } else if (colorType == 0) { // 灰度
                        int v = currLine[x] & 0xFF;
                        r = g = b = v;
                    }
                    pixels[y * width + x] = (a << 24) | (r << 16) | (g << 8) | b;
                }

                System.arraycopy(currLine, 0, prevLine, 0, width * bpp);
            }

            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB, pixels);
        } catch (Exception e) {
            return null;
        }
    }

    private static int paeth(int a, int b, int c) {
        int p = a + b - c;
        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);
        if (pa <= pb && pa <= pc) return a;
        if (pb <= pc) return b;
        return c;
    }

    private static int readInt(byte[] data, int pos) {
        return ((data[pos] & 0xFF) << 24) | ((data[pos + 1] & 0xFF) << 16)
             | ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF);
    }
}
