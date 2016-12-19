package api.packets.server;

import api.packets.Callback;
import api.packets.MessengerClient;
import api.packets.OutPacket;
import api.utils.Wrapper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by loucass003 on 07/12/16.
 */
public class RequestTPSPacket extends OutPacket
{
	private static final RequestTPSPacket CACHE = new RequestTPSPacket(); //Cache because no needs to re-instantiate
	// this class
	private static final Lock LOCK = new ReentrantLock();

	public static void request(MessengerClient client, Callback<byte[]> callback) throws IOException
	{
		short transaction = client.sendPacket(CACHE);

		client.listenPacket(SendTPSPacket.class, transaction, tps -> callback.response(tps.getLastTPS()));
	}

	public static byte[] request(MessengerClient client) throws IOException, InterruptedException
	{
		return request(client, 0, null);
	}

	public static byte[] request(MessengerClient client, long timeout, TimeUnit unit) throws IOException,
			InterruptedException
	{
		short transaction = client.sendPacket(CACHE);
		Condition condition = LOCK.newCondition();
		Wrapper<SendTPSPacket> out = new Wrapper<>();

		client.listenPacket(SendTPSPacket.class, transaction, sent ->
				{
					out.set(sent);
					condition.signalAll();
				}
		);

		if (timeout == 0) //Wait until the packet arrived
			condition.await();
		else
			condition.await(timeout, unit);

		return out.get().getLastTPS();
	}
}
