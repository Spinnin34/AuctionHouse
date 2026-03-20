package com.spawnchunk.auctionhouse.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUtil {
    public static boolean backupFile(File fn) {
        try {
            Path source = Paths.get(fn.getAbsolutePath(), new String[0]);
            Path backup = Paths.get(fn.getAbsolutePath() + ".backup", new String[0]);
            Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }
}

