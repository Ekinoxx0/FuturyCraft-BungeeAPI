package api.utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Comparator;
import java.util.UUID;
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

	public static boolean isReachable()
	{
		return true;
	}

	public static String intToString(int i)
	{
		ByteBuffer buf = ByteBuffer.wrap(new byte[4]);
		buf.putInt(i);
		return new String(buf.array());
	}

	public static int stringToInt(String str)
	{
		ByteBuffer buf = ByteBuffer.wrap(str.getBytes());
		return buf.getInt();
	}

	public static UUID base64ToUUID(String base64)
	{
		ByteBuffer buf = ByteBuffer.wrap(Base64.getUrlDecoder().decode((base64 + "==").getBytes()));
		return new UUID(buf.getLong(), buf.getLong());
	}

	public static String uuidToBase64(UUID uuid)
	{
		ByteBuffer buf = ByteBuffer.wrap(new byte[16]);
		buf.putLong(uuid.getMostSignificantBits());
		buf.putLong(uuid.getLeastSignificantBits());
		String str = Base64.getUrlEncoder().encodeToString(buf.array());
		return str.substring(0, str.length() - 2);
	}
}
