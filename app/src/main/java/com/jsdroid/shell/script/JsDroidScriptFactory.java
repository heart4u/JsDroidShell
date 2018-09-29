package com.jsdroid.shell.script;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class JsDroidScriptFactory {
    public static JsDroidScript create(ClassLoader classLoader, File scriptDir, File dexDir, String scriptText) throws Exception {
        dexDir.mkdir();
        dexDir.setExecutable(true);
        dexDir.setReadable(true);
        dexDir.setWritable(true);
        Compiler compiler = new Compiler(classLoader, scriptDir, dexDir);

        File mainFile = new File(scriptDir, "main.groovy");
        if (scriptText == null) {
            scriptText = FileUtils.readFileToString(mainFile, "utf-8");
        }
        return compiler.evaluate(scriptText, mainFile.getPath(), true);
    }
}
