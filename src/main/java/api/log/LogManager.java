package api.log;

import api.Main;
import api.data.Server;
import api.utils.SimpleManager;
import api.utils.Utils;
import api.utils.concurrent.ThreadLoop;
import api.utils.concurrent.ThreadLoops;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 07/01/2017.
 */
public final class LogManager implements SimpleManager
{
	private static final int SENDER_DELAY = 1000 * 60 * 60;
	private final ThreadLoop senderLoop = setupSenderExecutor();
	private boolean init;
	private volatile boolean end;
	private Path tmpDir;

	@Override
	public void init()
	{
		File file = new File(Main.getInstance().getDeployer().getConfig().getBaseDir(), "logs");
		if (!file.exists() && file.mkdirs())
			throw new IllegalStateException("Cannot mkdirs file " + file + '.');
		tmpDir = file.toPath();
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
						() -> {},
						getInitialDelay(),
						SENDER_DELAY,
						TimeUnit.MILLISECONDS
				);
	}

	private long getInitialDelay()
	{
		LocalTime now = LocalTime.now();
		LocalTime h1 = now.withSecond(0).withMinute(0).plusHours(1);
		return Duration.between(now, h1).toMillis();
	}

	public void saveLogs(Server server)
	{
		Path path = server.getDeployer().getLog();
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(tmpDir.resolve(server.getBase64UUID() + ".info"))))
		{
			Files.copy(path, tmpDir.resolve(server.getBase64UUID() + ".log"));
			writer.println(server.getName());
			writer.println(server.getOffset());
			writer.println(server.getDeployer().getPort());
			writer.println(server.getDeployer().getType());
			writer.println(server.getDeployer().getVariant());
		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while saving logs (Server: " +
					server + ')', e);
		}
	}

	public boolean checkUsedUUID(UUID uuid)
	{
		try
		{
			return Files.walk(tmpDir).anyMatch(path -> path.startsWith(Utils.uuidToBase64(uuid)));
		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while saving logs (UUID: " +
					uuid + ')', e);
			return true;
		}
	}
}
