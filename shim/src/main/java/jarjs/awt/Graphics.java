package jarjs.awt;

import org.teavm.jso.canvas.CanvasRenderingContext2D;

public class Graphics {
    private final CanvasRenderingContext2D ctx;
    private Color color = Color.BLACK;
    private Font font = new Font("sans-serif", Font.PLAIN, 14);

    public Graphics(CanvasRenderingContext2D ctx) {
        this.ctx = ctx;
        ctx.setLineWidth(1);
    }

    public CanvasRenderingContext2D raw() { return ctx; }

    public void setColor(Color c) {
        this.color = c == null ? Color.BLACK : c;
        ctx.setFillStyle(this.color.css());
        ctx.setStrokeStyle(this.color.css());
    }

    public Color getColor() { return color; }

    public void setFont(Font f) {
        this.font = f;
        ctx.setFont(f.css());
    }

    public Font getFont() { return font; }

    public void setLineWidth(double w) { ctx.setLineWidth(w); }

    public void fillRect(int x, int y, int w, int h) { ctx.fillRect(x, y, w, h); }

    public void clearRect(int x, int y, int w, int h) { ctx.clearRect(x, y, w, h); }

    public void drawRect(int x, int y, int w, int h) { ctx.strokeRect(x + 0.5, y + 0.5, w, h); }

    public void fillOval(int x, int y, int w, int h) {
        ctx.beginPath();
        ctx.ellipse(x + w / 2.0, y + h / 2.0, w / 2.0, h / 2.0, 0, 0, Math.PI * 2);
        ctx.fill();
    }

    public void drawOval(int x, int y, int w, int h) {
        ctx.beginPath();
        ctx.ellipse(x + w / 2.0, y + h / 2.0, w / 2.0, h / 2.0, 0, 0, Math.PI * 2);
        ctx.stroke();
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        ctx.beginPath();
        ctx.moveTo(x1 + 0.5, y1 + 0.5);
        ctx.lineTo(x2 + 0.5, y2 + 0.5);
        ctx.stroke();
    }

    public void drawString(String s, int x, int y) {
        if (s != null) ctx.fillText(s, x, y);
    }

    public void fillRoundRect(int x, int y, int w, int h, int arc) {
        ctx.beginPath();
        double r = Math.max(0, Math.min(arc, Math.min(w, h) / 2.0));
        ctx.moveTo(x + r, y);
        ctx.lineTo(x + w - r, y);
        ctx.quadraticCurveTo(x + w, y, x + w, y + r);
        ctx.lineTo(x + w, y + h - r);
        ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
        ctx.lineTo(x + r, y + h);
        ctx.quadraticCurveTo(x, y + h, x, y + h - r);
        ctx.lineTo(x, y + r);
        ctx.quadraticCurveTo(x, y, x + r, y);
        ctx.fill();
    }

    // ================= v0.6 补齐：图片绘制与资源释放 =================

    /** 绘制 BufferedImage（通过内部 Canvas 中转）。 */
    public void drawImage(jarjs.awt.image.BufferedImage img, int x, int y, Object observer) {
        if (img == null) return;
        org.teavm.jso.dom.html.HTMLCanvasElement cv =
                (org.teavm.jso.dom.html.HTMLCanvasElement) org.teavm.jso.browser.Window.current()
                        .getDocument().createElement("canvas");
        cv.setWidth(img.getWidth());
        cv.setHeight(img.getHeight());
        org.teavm.jso.canvas.CanvasRenderingContext2D c2 =
                (org.teavm.jso.canvas.CanvasRenderingContext2D) cv.getContext("2d");
        org.teavm.jso.canvas.ImageData data = c2.createImageData(img.getWidth(), img.getHeight());
        int[] px = img.getRawPixels();
        for (int i = 0; i < px.length; i++) {
            int v = px[i];
            data.getData().set(i * 4, (v >> 16) & 0xFF);
            data.getData().set(i * 4 + 1, (v >> 8) & 0xFF);
            data.getData().set(i * 4 + 2, v & 0xFF);
            data.getData().set(i * 4 + 3, (v >>> 24) & 0xFF);
        }
        c2.putImageData(data, 0, 0);
        ctx.drawImage(cv, x, y);
    }

    public void drawImage(Image img, int x, int y, Object observer) {
        if (img instanceof Image.BufferedImageImage) {
            drawImage(((Image.BufferedImageImage) img).image, x, y, observer);
        }
    }

    public void dispose() {}

    public Graphics create() { return this; }
}
