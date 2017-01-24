package api.utils;

import org.apache.commons.net.util.Base64;

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
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * Created by loucass003 on 15/12/16.
 */
public enum Utils
{
	;

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


