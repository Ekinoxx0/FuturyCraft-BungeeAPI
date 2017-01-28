package api.utils;

import org.apache.commons.net.util.Base64;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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

	public static <R> R doLocked(ReturnAction<R> action, Lock lock)
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
		ZipFile archive = new ZipFile(zip);

		for (Enumeration e = archive.entries(); e.hasMoreElements(); )
		{
			ZipEntry entry = (ZipEntry) e.nextElement();
			File file = new File(extractTo, entry.getName());

			if (entry.isDirectory() && !file.exists() && !file.mkdirs())
				throw new IOException("Cannot mkdirs directory");

			else
			{
				if (!file.getParentFile().exists() && !file.getParentFile().mkdirs())
					throw new IOException("Cannot mkdirs directory");

				InputStream in = archive.getInputStream(entry);
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(file));

				byte[] buffer = new byte[8192];
				int read;

				while (-1 != (read = in.read(buffer)))
				{
					out.write(buffer, 0, read);
				}
				in.close();
				out.close();
			}
		}
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


