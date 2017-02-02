package api.utils;

import api.Main;
import org.apache.commons.net.util.Base64;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by loucass003 on 15/12/16.
 */
public final class Utils
{
	private Utils() {}

	public static final InetAddress LOCAL_HOST = getLocalHost();

	private static InetAddress getLocalHost()
	{
		try
		{
			return InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
		}
		catch (UnknownHostException e)
		{
			throw new IllegalStateException(e);
		}
	}

	@FunctionalInterface
	public interface Action
	{
		void perform();
	}

	@FunctionalInterface
	public interface ReturnAction<R>
	{
		R perform();
	}

	@FunctionalInterface
	public interface ArgAction<A>
	{
		void perform(A arg);
	}

	@FunctionalInterface
	public interface ReturnArgAction<R, A>
	{
		R perform(A arg);
	}

	public static void doLocked(Action action, Lock lock)
	{
		lock.lock();
		try
		{
			action.perform();
		}
		finally
		{
			lock.unlock();
		}
	}

	public static <R> R returnLocked(ReturnAction<R> action, Lock lock)
	{
		lock.lock();
		try
		{
			return action.perform();
		}
		finally
		{
			lock.unlock();
		}
	}

	public static <R> R returnRedis(ReturnArgAction<R, Jedis> action)
	{
		try (Jedis jedis = Main.getInstance().getJedisPool().getResource())
		{
			return action.perform(jedis);
		}
	}

	public static void doRedis(ArgAction<Jedis> action)
	{
		try (Jedis jedis = Main.getInstance().getJedisPool().getResource())
		{
			action.perform(jedis);
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
			throw new IllegalStateException(e);
		}
	}

	public static void zipDirectory(File directory, File zip) throws IOException
	{
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
		zip(directory, directory, zos);
		zos.close();
	}

	private static void zip(File directory, File base,
	                        ZipOutputStream zos) throws IOException
	{
		File[] files = directory.listFiles();
		if (files == null) throw new IllegalArgumentException("File is not a directory");

		byte[] buffer = new byte[8192];
		for (File file : files)
		{
			if (file.isDirectory())
			{
				zip(file, base, zos);
			}
			else
			{
				FileInputStream in = new FileInputStream(file);
				ZipEntry entry = new ZipEntry(file.getPath().substring(
						base.getPath().length() + 1));
				zos.putNextEntry(entry);
				int read;
				while (-1 != (read = in.read(buffer)))
				{
					zos.write(buffer, 0, read);
				}
				in.close();
			}
		}
	}

	public static void unzip(File zip, File extractTo) throws IOException
	{
		if (!extractTo.exists())
		{
			extractTo.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zip));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null)
		{
			String filePath = extractTo + File.separator + entry.getName();
			if (!entry.isDirectory())
			{
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
				byte[] bytesIn = new byte[4096];
				int read = 0;
				while ((read = zipIn.read(bytesIn)) != -1)
				{
					bos.write(bytesIn, 0, read);
				}
				bos.close();
			}
			else
			{
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	public static Integer isNumeric(String str)
	{
		try
		{
			return Integer.valueOf(str);
		}
		catch (NumberFormatException ignored)
		{
			return null;
		}
	}

	public static void deleteContent(File f) throws IOException
	{
		Files.walk(f.toPath())
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
		if (str == null) return 0;

		ByteBuffer buf = ByteBuffer.wrap(str.getBytes());
		return buf.getInt();
	}

	public static String uuidToBase64(UUID uuid)
	{
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return Base64.encodeBase64URLSafeString(bb.array());
	}

	public static UUID uuidFromBase64(String str)
	{
		ByteBuffer bb = ByteBuffer.wrap(Base64.decodeBase64(str));
		return new UUID(bb.getLong(), bb.getLong());
	}
}


