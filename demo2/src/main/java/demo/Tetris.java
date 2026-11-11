package demo;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Tetris extends JFrame {
    public Tetris() {
        setTitle("JARJS Tetris");
        setSize(240, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Board board = new Board();
        add(board);
        board.start();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Tetris::new);
    }
}
