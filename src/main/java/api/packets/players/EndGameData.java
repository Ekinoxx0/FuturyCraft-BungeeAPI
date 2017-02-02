package api.packets.players;

import api.packets.IncPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SkyBeast on 02/02/17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class EndGameData extends IncPacket
{
	private final List<RelativeData> players = new ArrayList<>();

	public EndGameData(DataInputStream data) throws IOException
	{
		super(data);

		short count = data.readShort();
		for (short i = 0; i < count; i++)
			players.add(new RelativeData(data));
	}

	@Data
	public static class RelativeData
	{
		private final long earnedFuturyCoins;
		private final long earnedTurfuryCoins;

		public RelativeData(DataInputStream data) throws IOException
		{
			earnedFuturyCoins = data.readLong();
			earnedTurfuryCoins = data.readLong();
		}
	}
}
