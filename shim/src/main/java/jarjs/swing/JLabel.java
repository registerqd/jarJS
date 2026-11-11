package jarjs.swing;

import jarjs.awt.Graphics;

public class JLabel extends JComponent {
    private String text = "";

    public JLabel() {}
    public JLabel(String text) { this.text = text == null ? "" : text; }

    public void setText(String t) { text = t == null ? "" : t; }
    public String getText() { return text; }

    @Override
    public void paint(Graphics g) {
        if (background != null) {
            g.setColor(background);
            g.fillRect(x, y, width, height);
        }
        if (!text.isEmpty()) {
            g.setColor(foreground);
            g.setFont(font);
            g.drawString(text, x + 4, y + height / 2 + font.getSize() / 3);
        }
    }
}
