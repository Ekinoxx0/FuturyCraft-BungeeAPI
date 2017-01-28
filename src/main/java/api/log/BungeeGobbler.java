package api.log;

import api.events.NewBungeeConsoleLineEvent;
import api.events.NewConsoleLineEvent;
import api.utils.SimpleManager;
import net.md_5.bungee.api.ProxyServer;

import java.util.*;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Created by loucass003 on 1/28/17.
 */
public class BungeeGobbler extends StreamHandler implements SimpleManager
{
	private final Collection<String> lines;
	private final int maxLines;

	public BungeeGobbler()
	{
		lines = new ArrayDeque<>();
		maxLines = 250;
	}

	@Override
	public void init()
	{
		ProxyServer.getInstance().getLogger().addHandler(this);
	}

	@Override
	public void publish(LogRecord logRecord)
	{
		super.publish(logRecord);
		if(lines.size() == maxLines)
			((ArrayDeque<String>) lines).removeFirst();
		lines.add(logRecord.getMessage());
		ProxyServer.getInstance().getPluginManager().callEvent(new NewBungeeConsoleLineEvent(logRecord.getMessage()));
	}

	@Override
	public void flush()
	{
		super.flush();
	}

	@Override
	public void close() throws SecurityException
	{
		super.close();
	}

	public String getConsole()
	{
		return String.join("\n", lines);
	}
}
