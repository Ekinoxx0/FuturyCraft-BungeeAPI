package api.packets;

import api.Main;
import api.data.Server;
import api.events.PacketReceivedEvent;
import api.utils.SimpleManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.rabbitmq.client.*;
import lombok.ToString;
import lombok.extern.java.Log;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Event;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 17/12/2016.
 */
@ToString
@Log
public final class ServerMessenger implements SimpleManager
{
	private static final String INCOMING_QUEUE = "BungeeCord";
	private static final String OUTGOING_EXCHANGER = "servers";
	private Connection connection;
	private Channel channel;
	private MessageHandler consumer;

	@Override
	public void init()
	{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try
		{
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(INCOMING_QUEUE, false, false, false, null);
			consumer = new MessageHandler();
			channel.basicConsume(INCOMING_QUEUE, true, consumer);
		}
		catch (IOException | TimeoutException e)
		{
			throw new IllegalStateException("Cannot start ServerMessenger", e);
		}
	}

	/**
	 * Send a packet.
	 *
	 * @param serverID the server which will receive the packet.
	 * @param packet   the packet to send
	 */
	public void sendPacket(String serverID, OutPacket packet)
	{
		try
		{
			send(serverID, serializePacket(packet));
		}
		catch (IOException e)
		{
			log.log(Level.SEVERE, "Cannot send packet to server id " + serverID + " and packet " + packet + '.', e);
		}
	}

	/**
	 * Serialize a packet with its header.
	 *
	 * @param packet the packet
	 * @return the serialized packet
	 * @throws IOException i/o related method
	 */
	private static byte[] serializePacket(OutPacket packet)
			throws IOException
	{
		// Header
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeByte(Packets.getId(packet.getClass()));

		// Packet
		byte[] raw = serializeRawPacket(packet);
		out.writeInt(raw.length);
		out.write(raw);

		return out.toByteArray();
	}

	/**
	 * Serialize a raw packet without header.
	 *
	 * @param packet the packet to serialize
	 * @return the serialized packet
	 * @throws IOException i/o related method
	 */
	private static byte[] serializeRawPacket(OutPacket packet)
			throws IOException
	{
		ByteArrayDataOutput packetOut = ByteStreams.newDataOutput();
		packet.write(packetOut);
		return packetOut.toByteArray();
	}

	/**
	 * Send a packet to the exchanger.
	 *
	 * @param serverID the server id
	 * @param message  the serialized packet
	 * @throws IOException i/o related method
	 */
	private void send(String serverID, byte[] message)
			throws IOException
	{
		channel.basicPublish(OUTGOING_EXCHANGER, serverID, null, message);
	}

	@Override
	public void stop()
	{
		try
		{
			channel.close();
			connection.close();
		}
		catch (IOException | TimeoutException ignored) {}
	}

	/**
	 * Receive packets from the servers.
	 */
	private class MessageHandler extends DefaultConsumer
	{
		MessageHandler()
		{
			super(channel);
		}

		@Override
		public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
		                           byte[] body)
				throws IOException
		{
			ByteArrayDataInput in = ByteStreams.newDataInput(body);

			//Header
			String serverID = in.readUTF();

			//Packet
			InPacket packet = deserializePacket(in);

			Server server = Main.getInstance().getServerDataManager().getServer(serverID);

			callEvent(new PacketReceivedEvent(null, packet));
		}
	}

	/**
	 * Call an event.
	 *
	 * @param event the event
	 * @param <T>   the event type
	 * @return the event
	 */
	private static <T extends Event> T callEvent(T event)
	{
		return ProxyServer.getInstance().getPluginManager().callEvent(event);
	}

	/**
	 * Deserialize a packet with its header.
	 * @param in the data of the packet
	 * @return the packet
	 * @throws IOException i/o related method
	 */
	private static InPacket deserializePacket(ByteArrayDataInput in)
			throws IOException
	{
		byte id = in.readByte();
		int len = in.readInt();
		byte[] array = new byte[len];
		in.readFully(array);

		return deserializeRawPacket(id, array);
	}

	/**
	 * Deserialize a raw packet without its header.
	 * @param id the id of the packet
	 * @param in the data of the packet
	 * @return the packet
	 * @throws IOException i/o related method
	 */
	private static InPacket deserializeRawPacket(byte id, byte[] in)
			throws IOException
	{
		try
		{
			return Packets.constructIncomingPacket(id, ByteStreams.newDataInput(in));
		}
		catch (ReflectiveOperationException e)
		{
			throw new IOException("Cannot read incoming packet with id " + id + " and data " + Arrays.toString(in) +
					'.', e);
		}
	}
}
