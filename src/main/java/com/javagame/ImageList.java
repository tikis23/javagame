package com.javagame;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

final public class ImageList {
    public static final int TEXTURE_SIZE = 64;
    public static String[] get() {
        File folder = Paths.get("textures/").toFile();
        if (!folder.exists() || !folder.isDirectory()) {
            return null;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        String[] names = new String[files.length];
        int counter = 0;
        for (int i = 0; i < names.length; i++) {
            try {
                if (files[i].isFile() && (files[i].getName().toLowerCase().endsWith(".png") ||
                    files[i].getName().toLowerCase().endsWith(".jpg"))) {
                    names[counter] = files[i].getName();
                    counter++;
                }
            } catch (Exception e) {}
        }
        if (counter == 0) return null;
        Arrays.sort(names, new FileNameComparator());
        String[] newNames = new String[counter];
        for (int i = 0; i < counter; i++) {
            newNames[i] = Paths.get("textures/" + names[i]).toString();
        }
        names = newNames;
        return names;
    }

    private static class FileNameComparator implements Comparator<String> {
        public int compare(String f1, String f2) {
            String[] p1 = f1.split("\\.");
            String[] p2 = f2.split("\\.");
    
            int n1 = Integer.parseInt(p1[0]);
            int n2 = Integer.parseInt(p2[0]);

            return Integer.compare(n1, n2);
        }
    }
}
