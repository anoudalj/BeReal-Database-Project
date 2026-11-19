import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Shared Swing styling; no extra UI libraries are required. */
public final class BeRealTheme {
    public static final Color BG = Color.BLACK;
    public static final Color SURFACE = new Color(22, 22, 22);
    public static final Color LINE = new Color(49, 49, 49);
    public static final Color MUTED = new Color(160, 160, 160);
    private static boolean installed;
    private BeRealTheme() { }

    public static void install() {
        if (installed) return;
        installed = true;
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) { }
        Font font = new Font("SansSerif", Font.PLAIN, 14);
        for (Object key : UIManager.getDefaults().keySet().toArray()) {
            if (key.toString().endsWith(".font")) UIManager.put(key, font);
        }
        for (String name : new String[]{"Panel", "OptionPane", "Viewport", "TabbedPane", "ScrollPane"}) {
            UIManager.put(name + ".background", BG);
            UIManager.put(name + ".foreground", Color.WHITE);
        }
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("PasswordField.background", SURFACE);
        UIManager.put("PasswordField.foreground", Color.WHITE);
        UIManager.put("TextArea.background", SURFACE);
        UIManager.put("TextArea.foreground", Color.WHITE);
        UIManager.put("TextField.caretForeground", Color.WHITE);
        UIManager.put("PasswordField.caretForeground", Color.WHITE);
        UIManager.put("ComboBox.background", SURFACE);
        UIManager.put("ComboBox.foreground", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", LINE);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("TabbedPane.selected", SURFACE);
        UIManager.put("TabbedPane.contentAreaColor", BG);
        UIManager.put("Table.background", BG);
        UIManager.put("Table.foreground", Color.WHITE);
        UIManager.put("Table.selectionBackground", LINE);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", SURFACE);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("Button.background", SURFACE);
        UIManager.put("Button.foreground", Color.WHITE);
    }

    public static JPanel panel(LayoutManager layout) {
        JPanel p = new JPanel(layout); p.setBackground(BG); return p;
    }
    public static JLabel label(String text, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(Color.WHITE); return l;
    }
    public static JLabel note(String text) {
        JLabel l = label(text, 13, false); l.setForeground(MUTED); return l;
    }
    public static JPanel header(String subtitle) {
        JPanel p = panel(new BorderLayout(0, 12));
        JLabel brand = label("BeReal.", 31, true);
        brand.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(brand, BorderLayout.NORTH);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel s = note(subtitle); s.setHorizontalAlignment(SwingConstants.CENTER);
            p.add(s, BorderLayout.SOUTH);
        }
        return p;
    }
    public static JButton button(String text, boolean primary) {
        return new RoundedButton(text, primary);
    }
    public static void input(JTextComponent f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 15));
        f.setBackground(SURFACE); f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(new CompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(12, 14, 12, 14)));
    }
    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(null); s.getViewport().setBackground(BG);
        s.getVerticalScrollBar().setUnitIncrement(22); return s;
    }
    /** Applies the same palette to the original project forms and tables. */
    public static void apply(Component component) {
        if (component instanceof JPanel) component.setBackground(BG);
        if (component instanceof JLabel) component.setForeground(Color.WHITE);
        if (component instanceof JTextComponent) input((JTextComponent) component);
        if (component instanceof JButton && !(component instanceof RoundedButton)) {
            JButton b = (JButton) component;
            b.setUI(new BasicButtonUI()); b.setBackground(SURFACE); b.setForeground(Color.WHITE);
            b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setBorder(new CompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(10, 14, 10, 14)));
        }
        if (component instanceof JComponent) {
            Border border = ((JComponent) component).getBorder();
            if (border instanceof TitledBorder) ((TitledBorder) border).setTitleColor(MUTED);
        }
        if (component instanceof JTable) {
            JTable t = (JTable) component;
            t.setRowHeight(38); t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0, 0));
            t.setBackground(BG); t.setForeground(Color.WHITE);
            t.setSelectionBackground(LINE); t.setSelectionForeground(Color.WHITE);
            t.getTableHeader().setBackground(SURFACE); t.getTableHeader().setForeground(Color.WHITE);
            t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        }
        if (component instanceof JComboBox) {
            component.setBackground(SURFACE); component.setForeground(Color.WHITE);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) apply(child);
        }
    }

    public static final class RoundedButton extends JButton {
        private final boolean primary;
        RoundedButton(String text, boolean primary) {
            super(text); this.primary = primary;
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setForeground(primary ? Color.BLACK : Color.WHITE);
            setBackground(primary ? Color.WHITE : SURFACE);
            setContentAreaFilled(false); setOpaque(false); setBorderPainted(false);
            setFocusPainted(false); setBorder(new EmptyBorder(13, 22, 13, 22));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getModel().isPressed() ? (primary ? new Color(210, 210, 210) : LINE) : getBackground());
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
            if (hasFocus()) { g.setColor(MUTED); g.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 23, 23); }
            g.dispose(); super.paintComponent(graphics);
        }
    }

    /** A rear photo with the front camera inset, using the project's local image paths. */
    public static final class DualPhoto extends JComponent {
        private BufferedImage back, front;
        private final String emptyText;
        public DualPhoto(String backPath, String frontPath, String emptyText) {
            this.emptyText = emptyText;
            setPhotos(backPath, frontPath);
            setPreferredSize(new Dimension(430, 480));
            setMinimumSize(new Dimension(240, 270));
        }
        public void setPhotos(String backPath, String frontPath) {
            back = read(backPath); front = read(frontPath); repaint();
        }
        private BufferedImage read(String path) {
            if (path == null || path.isBlank()) return null;
            try { return ImageIO.read(new File(path)); } catch (Exception e) { return null; }
        }
        private void photo(Graphics2D g, BufferedImage image, int x, int y, int w, int h, int radius) {
            Shape old = g.getClip();
            g.clip(new RoundRectangle2D.Double(x, y, w, h, radius, radius));
            g.setColor(SURFACE); g.fillRect(x, y, w, h);
            if (image != null) {
                double scale = Math.max((double)w / image.getWidth(), (double)h / image.getHeight());
                int iw = (int)Math.ceil(image.getWidth()*scale), ih = (int)Math.ceil(image.getHeight()*scale);
                g.drawImage(image, x+(w-iw)/2, y+(h-ih)/2, iw, ih, null);
            }
            g.setClip(old);
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int w = getWidth(), h = getHeight(); photo(g, back, 0, 0, w, h, 28);
            if (back == null) {
                g.setColor(MUTED); g.setFont(new Font("SansSerif", Font.PLAIN, 14));
                FontMetrics fm = g.getFontMetrics(); g.drawString(emptyText, Math.max(12,(w-fm.stringWidth(emptyText))/2), h/2);
            }
            int iw = Math.max(72, w/4), ih = (int)(iw*1.3);
            g.setColor(Color.WHITE); g.fillRoundRect(13, 13, iw+4, ih+4, 18, 18);
            photo(g, front, 15, 15, iw, ih, 15);
            if (front == null) {
                g.setColor(MUTED); g.setFont(new Font("SansSerif", Font.PLAIN, 11)); g.drawString("Front camera", 22, 38);
            }
            g.dispose();
        }
    }
}
