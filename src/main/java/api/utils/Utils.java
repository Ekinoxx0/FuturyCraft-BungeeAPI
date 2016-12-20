package api.utils;

import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Created by loucass003 on 15/12/16.
 */
public class Utils {

    public static String readFile(File f)
    {
        try
        {
            return Files.readAllLines(f.toPath()).stream().collect(Collectors.joining("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer isNumeric(String str)
    {
        try
        {
            return Integer.valueOf(str);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static void deleteContent(File f) throws IOException
    {
        Files.walk(Paths.get(f.getAbsolutePath()))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static void deleteFolder(File f) throws IOException
    {
        Path rootPath = Paths.get(f.getAbsolutePath());
        Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
