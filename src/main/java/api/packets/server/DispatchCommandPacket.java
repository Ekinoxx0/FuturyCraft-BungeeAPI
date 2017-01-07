package api.packets.server;

import api.packets.OutPacket;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 05/01/2017.
 */
public class DispatchCommandPacket extends OutPacket
{
	private final String command;

	public DispatchCommandPacket(String command)
	{
		this.command = command;
	}

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeUTF(command);
	}

	public String getCommand()
	{
		return command;
	}

	@Override
	public String toString()
	{
		return "DispatchCommandPacket{" +
				"command='" + command + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DispatchCommandPacket that = (DispatchCommandPacket) o;

		return command != null ? command.equals(that.command) : that.command == null;

	}

	@Override
	public int hashCode()
	{
		return command != null ? command.hashCode() : 0;
	}
}
