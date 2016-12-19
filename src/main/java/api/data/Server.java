package api.data;

import api.packets.MessengerClient;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Created by SkyBeast on 19/12/2016.
 */
public class Server
{
	private MessengerClient messenger;
	private final ServerTemplate template;
	private final ServerInfo info;

	public Server(MessengerClient messenger, ServerTemplate template, ServerInfo info)
	{
		this.messenger = messenger;
		this.template = template;
		this.info = info;
	}

	public MessengerClient getMessenger()
	{
		return messenger;
	}

	void setMessenger(MessengerClient messenger)
	{
		this.messenger = messenger;
	}

	public ServerTemplate getTemplate()
	{
		return template;
	}

	public ServerInfo getInfo()
	{
		return info;
	}

	@Override
	public String toString()
	{
		return "Server{" +
				"messenger=" + messenger +
				", template=" + template +
				", info=" + info +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Server server = (Server) o;

		if (messenger != null ? !messenger.equals(server.messenger) : server.messenger != null) return false;
		if (template != null ? !template.equals(server.template) : server.template != null) return false;
		return info != null ? info.equals(server.info) : server.info == null;

	}

	@Override
	public int hashCode()
	{
		int result = messenger != null ? messenger.hashCode() : 0;
		result = 31 * result + (template != null ? template.hashCode() : 0);
		result = 31 * result + (info != null ? info.hashCode() : 0);
		return result;
	}
}
