package com.jsdroid.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ApkUtil {


    public static void zipLib(File apkFile, File outDir) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(apkFile));
        File libDir = new File(outDir, "lib");
        libDir.mkdir();
        for (; ; ) {
            ZipEntry nextEntry = zipInputStream.getNextEntry();
            if (nextEntry == null) {
                break;
            }
            String name = nextEntry.getName();
            if (name.startsWith("lib/")) {
                if (nextEntry.isDirectory()) {
                    new File(outDir, name).mkdir();
                } else {
                    File outFile = new File(outDir, name);
                    outFile.getParentFile().mkdirs();
                    FileOutputStream fileOutputStream = new FileOutputStream(outFile);
                    IOUtils.copy(zipInputStream, fileOutputStream);
                    fileOutputStream.close();
                }
            }

        }
        zipInputStream.close();
    }

    public static void main(String[] args) {
        new File("out").mkdir();
        try {
            zipLib(new File("test.apk"), new File("out"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
