package api.log;

import api.Main;
import api.data.Server;
import api.utils.SimpleManager;
import api.utils.Utils;
import api.utils.concurrent.ThreadLoop;
import api.utils.concurrent.ThreadLoops;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 07/01/2017.
 */
public final class LogManager implements SimpleManager
{
	private static final String FTP_HOST = "dedibackup-dc3.online.net:22";
	private static final String FTP_USER = "sd-112484";
	private static final String FTP_PASSWORD = "cR7Ay3eIJLx4";
	private static final int SENDER_DELAY = 1000 * 60 * 60;
	private final DateFormat dateFormat; //dd-MMM-yyyy
	private final DateFormat timeFormat; //HH:mm:ss
	private final ThreadLoop senderLoop = setupSenderExecutor();
	private boolean init;
	private volatile boolean end;
	private Path logsDir;

	public LogManager()
	{
		dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));
		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));
	}

	@Override
	public void init()
	{
		File file = new File(Main.getInstance().getDeployer().getConfig().getBaseDir(), "logs");
		if (!file.exists() && !file.mkdirs())
			throw new IllegalStateException("Cannot mkdirs file " + file + '.');
		logsDir = file.toPath();
		if (init)
			throw new IllegalStateException("Already initialized!");

		//setupSenderExecutor();

		init = true;
	}

	@Override
	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");

		end = true;

		Main.getInstance().getLogger().info(this + " stopped.");
	}

	private ThreadLoop setupSenderExecutor()
	{
		return ThreadLoops.newScheduledThreadLoop
				(
						() ->
						{
							try
							{
								sendLogs();
							}
							catch (IOException e)
							{
								Main.getInstance().getLogger().log(Level.SEVERE, "Error while sending logs", e);
							}
						},
						getInitialDelay(),
						SENDER_DELAY,
						TimeUnit.MILLISECONDS
				);
	}

	/**
	 * Send all logs to the FTP.
	 *
	 * @throws IOException uses FTPClient
	 */
	private synchronized void sendLogs() throws IOException
	{
		File zip = zipLogs();
		FTPClient ftp = new FTPClient();

		try
		{
			if (!authFTP(ftp))
				return;

			Date date = new Date();
			String fDate = dateFormat.format(date);
			ftp.makeDirectory("/logs/" + fDate);
			ftp.appendFile("/logs/" + fDate + '/' + timeFormat.format(date), new FileInputStream(zip));

			ftp.logout();
			ftp.disconnect();
		}
		finally
		{
			disconnectFTP(ftp);
		}
	}

	/**
	 * Zip all the logs.
	 *
	 * @return the zip file
	 * @throws IOException zip the file
	 */
	private File zipLogs() throws IOException
	{
		File zip = new File(Main.getInstance().getDeployer().getConfig().getBaseDir(), "logs.zip");
		Utils.zipDirectory(logsDir.toFile(), zip);
		return zip;
	}

	/**
	 * Auth the FTP client.
	 *
	 * @param ftp the client
	 * @return whether or not the auth was accepted
	 * @throws IOException uses FTPClient
	 */
	private boolean authFTP(FTPClient ftp) throws IOException
	{
		int reply = ftp.getReplyCode();

		if (!FTPReply.isPositiveCompletion(reply))
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "FTP server did not accept me ;ç Reply code: " +
					reply);
			ftp.disconnect();
			return false;
		}

		return true;
	}

	/**
	 * Disconnect a FTP client.
	 *
	 * @param ftp the client
	 */
	private void disconnectFTP(FTPClient ftp)
	{
		if (ftp.isConnected())
			try {ftp.disconnect();}
			catch (IOException ignored) {}
	}

	/**
	 * Get the duration between now and in the bext start of hour.
	 *
	 * @return a unix time
	 */
	private long getInitialDelay()
	{
		LocalTime now = LocalTime.now();
		LocalTime h1 = now.withSecond(0).withMinute(0).plusHours(1);
		return Duration.between(now, h1).toMillis();
	}

	/**
	 * Save all logs from a server.
	 *
	 * @param server the server
	 */
	public synchronized void saveLogs(Server server)
	{
		/*Path path = server.getDeployer().getLog();
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(logsDir.resolve(server.getId() +
				".info"))))
		{
			Files.copy(path, logsDir.resolve(server.getId() + ".log"));
			writer.println(server.getName());
			writer.println(server.getInfo().getAddress().getPort());
			writer.println(server.getType());
			writer.println(server.getVariant());
		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while saving logs (Server: " +
					server + ')', e);
		}*/
	}

	/**
	 * Check if a UUID was used in the last hour.
	 *
	 * @param id the uuid to check
	 * @return whether or not the uuid was used
	 */
	public boolean checkUsedID(String id)
	{
		try
		{
			return Files.walk(logsDir).anyMatch(path -> path.startsWith(id));
		}
		catch (IOException ignored)
		{
			return true;
		}
	}
}
