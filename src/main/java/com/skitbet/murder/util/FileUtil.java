package com.skitbet.murder.util;

import java.io.*;

public class FileUtil {

    public static void copy(File src, File des) throws IOException {
        if (src.isDirectory()) {
            if (!des.exists()) {
                des.mkdir();
            }

            String[] files = src.list();
            if (files == null) return;
            for (String file : files) {
                File newSource = new File(src,file);
                File newDestination = new File(des, file);
                copy(newSource, newDestination);
            }
        }else{
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(des);

            byte[] buffer = new byte[1024];

            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }

    public static void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File child : files) {
                delete(child);
            }
        }
        file.delete();
    }
}
