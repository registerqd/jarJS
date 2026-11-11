package demo;
import java.awt.Graphics;
import org.teavm.jso.dom.html.HTMLDocument;
public class Main {
    public static void main(String[] args) {
        Graphics g = new Graphics();
        g.fillRect(1, 2, 3, 4);
        HTMLDocument.current().getElementById("out").setTextContent("probe ok");
    }
}
