package api.packets.server;

import api.packets.OutPacket;
import lombok.Data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by loucass003 on 2/4/17.
 */
@Data
public class BossBarMessagesPacket extends OutPacket
{
	private final List<MessageData> messages;

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeShort(messages.size());
		for(MessageData data : messages)
			data.write(out);
	}

	@Data
	public static class MessageData
	{
		private final String message;
		private final int time;

		public void write(DataOutputStream out) throws IOException
		{
			out.writeUTF(message);
			out.writeInt(time);
		}
	}
}
