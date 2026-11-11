package jarjs.swing;

public final class SwingUtilities {
    private SwingUtilities() {}

    public static void invokeLater(Runnable r) {
        if (r != null) r.run();
    }

    public static void invokeAndWait(Runnable r) {
        if (r != null) r.run();
    }
}
