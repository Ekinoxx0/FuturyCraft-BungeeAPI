package api.packets.server;

import api.packets.IncPacket;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 20/12/2016.
 */
public class ServerStatePacket extends IncPacket
{
	private final ServerState serverState;

	public ServerStatePacket(DataInputStream data) throws IOException
	{
		super(data);
		this.serverState = ServerState.values()[data.readInt()];
	}

	public ServerState getServerState()
	{
		return serverState;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ServerStatePacket that = (ServerStatePacket) o;

		return serverState == that.serverState;

	}

	@Override
	public int hashCode()
	{
		return serverState != null ? serverState.hashCode() : 0;
	}

	@Override
	public String toString()
	{
		return "ServerStatePacket{" +
				"serverState=" + serverState +
				'}';
	}

	public enum ServerState
	{

		/**
		 * The server is starting
		 */
		STARTING,

		/**
		 * The server is started
		 */
		STARTED,

		/**
		 * The server is ready to accept players
		 */
		READY,

		/**
		 * NON-LOBBY
		 * The game is waiting for players
		 */
		WAITING_PLAYERS,

		/**
		 * NON-LOBBY
		 * The game is started
		 */
		GAME_STARTED,

		/**
		 * NON-LOBBY
		 * The game is ended but the server is not stopping yet
		 */
		GAME_ENDED,

		/**
		 * The server is stopping because of an error
		 */
		STOP_ERROR,

		/**
		 * The server is auto-stopping
		 */
		STOPPING,

		/**
		 * The server is stopping because we told him to do
		 */
		STOPPING_ASKED
	}
}
