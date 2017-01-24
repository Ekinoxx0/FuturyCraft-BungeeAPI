package api.log;

import api.Main;
import api.data.Server;
import api.utils.SimpleManager;
import api.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 07/01/2017.
 */
public class LogManager implements SimpleManager
{
	private static final int SENDER_DELAY = 1000 * 60 * 60;
	private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	private boolean init;
	private volatile boolean end;
	private final Path tmpDir;

	public LogManager()
	{
		File file = new File(Main.getInstance().getDeployer().getConfig().getBaseDir(), "logs");
		if (file.mkdirs())
			throw new IllegalStateException("Cannot mkdirs file " + file + '.');
		tmpDir = file.toPath();
	}

	@Override
	public void init()
	{
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

	private void setupSenderExecutor()
	{
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime h1 = now.withSecond(0).withMinute(0).plusHours(1);

		Duration duration = Duration.between(now, h1);
		exec.scheduleWithFixedDelay(
				() ->
				{

				},
				duration.toMillis(),
				SENDER_DELAY,
				TimeUnit.MILLISECONDS
		);
	}

	public void saveLogs(Server server)
	{
		Path path = server.getDeployer().getLog();
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(tmpDir.resolve(server.getBase64UUID() +
				".info"))))
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
			return Files.walk(tmpDir)
					.anyMatch(path -> path.startsWith(Utils.uuidToBase64(uuid)));
		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while saving logs (UUID: " +
					uuid + ')', e);
			return true;
		}
	}
}
