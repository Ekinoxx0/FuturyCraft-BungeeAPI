package api.utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by loucass003 on 15/12/16.
 */
public class Utils
{
	public static final InetAddress LOCAL_HOST = getLocalHost();
	private static final char[] C64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();
	private static final int[] I256 = new int[256];

	static
	{
		for (int i = 0; i < C64.length; i++)
		{
			I256[C64[i]] = i;
		}
	}

	private static InetAddress getLocalHost()
	{
		try
		{
			return InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
		}
		catch (UnknownHostException e)
		{
			throw new IllegalStateException();
		}
	}

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
		catch (NumberFormatException e)
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

	public static String formatToUUID(UUID uuid)
	{
		if (uuid == null) throw new NullPointerException("Null UUID");

		byte[] bytes = toByteArray(uuid);
		return encodeBase64(bytes);
	}

	public static UUID parseUUID(String uuidString)
	{
		if (uuidString == null) throw new NullPointerException("Null UUID string");

		if (uuidString.length() > 24)
		{
			return UUID.fromString(uuidString);
		}

		if (uuidString.length() < 22)
		{
			throw new IllegalArgumentException("Short UUID must be 22 characters: " + uuidString);
		}

		byte[] bytes = decodeBase64(uuidString);
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.put(bytes, 0, 16);
		bb.clear();
		return new UUID(bb.getLong(), bb.getLong());
	}

	private static byte[] toByteArray(UUID uuid)
	{
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}

	private static byte[] decodeBase64(String s)
	{
		if (s == null) throw new NullPointerException("Cannot decode null string");
		if (s.isEmpty() || (s.length() > 24)) throw new IllegalArgumentException("Invalid short UUID");

		// Output is always 16 bytes (UUID).
		byte[] bytes = new byte[16];
		int i = 0;
		int j = 0;

		while (i < 15)
		{
			// Get the next four characters.
			int d = I256[s.charAt(j++)] << 18 | I256[s.charAt(j++)] << 12 | I256[s.charAt(j++)] << 6 | I256[s.charAt
					(j++)];

			// Put them in these three bytes.
			bytes[i++] = (byte) (d >> 16);
			bytes[i++] = (byte) (d >> 8);
			bytes[i++] = (byte) d;
		}

		// Add the last two characters from the string into the last byte.
		bytes[i] = (byte) ((I256[s.charAt(j++)] << 18 | I256[s.charAt(j + 1)] << 12) >> 16);
		return bytes;
	}

	private static String encodeBase64(byte[] bytes)
	{
		if (bytes == null) throw new NullPointerException("Null UUID byte array");
		if (bytes.length != 16) throw new IllegalArgumentException("UUID must be 16 bytes");

		// Output is always 22 characters.
		char[] chars = new char[22];

		int i = 0;
		int j = 0;

		while (i < 15)
		{
			// Get the next three bytes.
			int d = (bytes[i++] & 0xff) << 16 | (bytes[i++] & 0xff) << 8 | (bytes[i++] & 0xff);

			// Put them in these four characters
			chars[j++] = C64[(d >>> 18) & 0x3f];
			chars[j++] = C64[(d >>> 12) & 0x3f];
			chars[j++] = C64[(d >>> 6) & 0x3f];
			chars[j++] = C64[d & 0x3f];
		}

		// The last byte of the input gets put into two characters at the end of the string.
		int d = (bytes[i] & 0xff) << 10;
		chars[j++] = C64[d >> 12];
		chars[j + 1] = C64[(d >>> 6) & 0x3f];
		return new String(chars);
	}
}


