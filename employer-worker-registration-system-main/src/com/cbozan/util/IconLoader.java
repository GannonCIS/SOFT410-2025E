package com.cbozan.util;

import javax.swing.ImageIcon;
import java.net.URL;
import java.io.File;

/**
 * Utility to load icons from classpath (/icon/) first, then fallback to src/icon/ file path.
 * Returns an empty ImageIcon if not found to avoid NPEs when icons are missing.
 */
public class IconLoader {

    public static ImageIcon load(String filename) {
        if (filename == null) return new ImageIcon();
        // try classpath
        URL url = IconLoader.class.getResource("/icon/" + filename);
        if (url != null) return new ImageIcon(url);
        // fallback to development file path
        File f = new File("src/icon/" + filename);
        if (f.exists()) return new ImageIcon(f.getPath());
        // missing icon: return empty icon so callers don't NPE
        return new ImageIcon();
    }
}
