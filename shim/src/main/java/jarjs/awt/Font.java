package jarjs.awt;

public class Font {
    public static final int PLAIN = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;

    private final String name;
    private final int style;
    private final int size;

    public Font(String name, int style, int size) {
        this.name = name == null ? "sans-serif" : name;
        this.style = style;
        this.size = size <= 0 ? 14 : size;
    }

    public String getName() { return name; }
    public int getStyle() { return style; }
    public int getSize() { return size; }

    public String css() {
        StringBuilder sb = new StringBuilder();
        if ((style & ITALIC) != 0) sb.append("italic ");
        if ((style & BOLD) != 0) sb.append("bold ");
        sb.append(size).append("px ").append(name);
        return sb.toString();
    }
}
