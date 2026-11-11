package demo3;

import com.example.missing.Foo;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;

public class Main extends JFrame {
    public Main() {
        setTitle("Lenient Test");
        setSize(320, 200);
        String s = Foo.bar(42);
        int sum = new Foo().baz(3, 4);
        JLabel label = new JLabel(s + "  sum=" + sum);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("sans-serif", Font.BOLD, 18));
        add(label);
        setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
