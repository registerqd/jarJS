package demo;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

class Board extends JPanel implements ActionListener {
    private static final int COLS = 10, ROWS = 20, SIZE = 16, TOP = 30;

    private static final int[][][] SHAPES = {
        {{1,1,1,1}},
        {{1,1},{1,1}},
        {{0,1,0},{1,1,1}},
        {{0,1,1},{1,1,0}},
        {{1,1,0},{0,1,1}},
        {{1,0,0},{1,1,1}},
        {{0,0,1},{1,1,1}}
    };
    private static final Color[] COLORS = {
        Color.CYAN, Color.YELLOW, Color.MAGENTA,
        Color.GREEN, Color.RED, Color.BLUE, Color.ORANGE
    };

    private final int[][] grid = new int[ROWS][COLS];
    private final Random rnd = new Random();
    private final ReentrantLock lock = new ReentrantLock();
    private final Timer timer;

    private int shape, rot, curX, curY, score, lines;
    private boolean paused, over;

    Board() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                onKey(e.getKeyCode());
            }
        });
        timer = new Timer(420, this);
        newPiece();
    }

    void start() { timer.start(); }

    private int[][] cells() {
        int[][] s = SHAPES[shape];
        int[][] r = s;
        for (int i = 0; i < rot; i++) r = rotate(r);
        return r;
    }

    private static int[][] rotate(int[][] s) {
        int h = s.length, w = s[0].length;
        int[][] r = new int[w][h];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                r[x][h - 1 - y] = s[y][x];
        return r;
    }

    private void newPiece() {
        shape = rnd.nextInt(SHAPES.length);
        rot = 0;
        curX = COLS / 2 - 1;
        curY = 0;
        if (collides(curX, curY, cells())) { over = true; timer.stop(); }
    }

    private boolean collides(int px, int py, int[][] c) {
        for (int y = 0; y < c.length; y++)
            for (int x = 0; x < c[0].length; x++) {
                if (c[y][x] == 0) continue;
                int gx = px + x, gy = py + y;
                if (gx < 0 || gx >= COLS || gy >= ROWS) return true;
                if (gy >= 0 && grid[gy][gx] != 0) return true;
            }
        return false;
    }

    private void lock() {
        int[][] c = cells();
        for (int y = 0; y < c.length; y++)
            for (int x = 0; x < c[0].length; x++)
                if (c[y][x] != 0) {
                    int gy = curY + y, gx = curX + x;
                    if (gy >= 0) grid[gy][gx] = shape + 1;
                }
        clearLines();
        newPiece();
    }

    private void clearLines() {
        int cleared = 0;
        for (int y = ROWS - 1; y >= 0; y--) {
            boolean full = true;
            for (int x = 0; x < COLS; x++) if (grid[y][x] == 0) { full = false; break; }
            if (full) {
                cleared++;
                for (int yy = y; yy > 0; yy--) grid[yy] = grid[yy - 1].clone();
                grid[0] = new int[COLS];
                y++;
            }
        }
        if (cleared > 0) {
            lines += cleared;
            score += cleared * cleared * 100;
        }
    }

    private void stepDown() {
        if (over || paused) return;
        lock.lock();
        try { stepDownLocked(); } finally { lock.unlock(); }
    }

    private void stepDownLocked() {
        int[][] c = cells();
        if (!collides(curX, curY + 1, c)) curY++;
        else lock();
    }

    private void onKey(int code) {
        if (code == KeyEvent.VK_P) { paused = !paused; return; }
        if (over || paused) return;
        int[][] c = cells();
        switch (code) {
            case KeyEvent.VK_LEFT:
                if (!collides(curX - 1, curY, c)) curX--;
                break;
            case KeyEvent.VK_RIGHT:
                if (!collides(curX + 1, curY, c)) curX++;
                break;
            case KeyEvent.VK_DOWN:
                stepDown();
                break;
            case KeyEvent.VK_UP:
                rot = (rot + 1) % 4;
                if (collides(curX, curY, cells())) rot = (rot + 3) % 4;
                break;
            case KeyEvent.VK_SPACE:
                while (!collides(curX, curY + 1, cells())) curY++;
                lock();
                break;
            default:
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        stepDown();
    }

    @Override
    public void paint(Graphics g) {
        lock.lock();
        try { paintLocked(g); } finally { lock.unlock(); }
    }

    private void paintLocked(Graphics g) {
        g.setColor(new Color(18, 22, 30));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(70, 80, 100));
        g.drawRect(0, TOP - 1, COLS * SIZE, ROWS * SIZE);

        for (int y = 0; y < ROWS; y++)
            for (int x = 0; x < COLS; x++) {
                if (grid[y][x] == 0) continue;
                drawCell(g, x, y, COLORS[(grid[y][x] - 1) % COLORS.length]);
            }

        if (!over) {
            int[][] c = cells();
            for (int y = 0; y < c.length; y++)
                for (int x = 0; x < c[0].length; x++)
                    if (c[y][x] != 0) drawCell(g, curX + x, curY + y, COLORS[shape]);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("sans-serif", Font.BOLD, 14));
        g.drawString("Score: " + score + "   Lines: " + lines, 6, 20);

        if (paused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("sans-serif", Font.BOLD, 20));
            g.drawString("PAUSED", COLS * SIZE / 2 - 45, TOP + ROWS * SIZE / 2);
        }
        if (over) {
            g.setColor(Color.RED);
            g.setFont(new Font("sans-serif", Font.BOLD, 20));
            g.drawString("GAME OVER", COLS * SIZE / 2 - 60, TOP + ROWS * SIZE / 2);
        }
    }

    private void drawCell(Graphics g, int x, int y, Color c) {
        g.setColor(c);
        g.fillRect(x * SIZE + 1, y * SIZE + TOP + 1, SIZE - 2, SIZE - 2);
    }
}
