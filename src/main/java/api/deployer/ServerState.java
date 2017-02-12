package api.deployer;

import lombok.AllArgsConstructor;

/**
 * Created by loucass003 on 2/12/17.
 */
@AllArgsConstructor
public enum ServerState
{

	/**
	 * The server is starting
	 */
	STARTING(false),

	/**
	 * The server is started
	 */
	STARTED(false),

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
	GAME_ENDED(false),

	/**
	 * The server is stopping because of an error
	 */
	STOPPING_ERROR(false),

	/**
	 * The server is auto-stopping
	 */
	STOPPING(false),

	/**
	 * The server is stopping because we told him to do
	 */
	STOPPING_ASKED(false),

	/**
	 * The server is stopped. Actually not sent but updated.
	 */
	STOPPED(false);

	public final boolean acceptPlayers;

	ServerState()
	{
		this(true);
	}

	public boolean canAcceptPlayers()
	{
		return acceptPlayers;
	}
}
