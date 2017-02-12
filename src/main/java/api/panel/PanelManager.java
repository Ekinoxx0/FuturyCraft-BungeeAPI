package api.panel;

import api.Main;
import api.events.*;
import api.packets.IncPacket;
import api.utils.SimpleManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by SkyBeast on 04/01/2017.
 */
@ToString
public final class PanelManager implements SimpleManager
{
	private final Listen listener = new Listen();
	@Getter
	@Setter(AccessLevel.PACKAGE)
	private MessengerPanel messengerPanel; //Only one instance
	private boolean init;

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialized!");

		Main.getInstance().getProxy().getPluginManager().registerListener(Main.getInstance(), listener);
		init = true;
	}

	/**
	 * Reset the listening state.
	 */
	public void resetListening()
	{

	}

	public class Listen implements Listener
	{
		private Listen() {}

		/*
		 * Handle all panel packets.
		 */
		@EventHandler
		public void onPanelPacket(PanelPacketReceivedEvent event)
		{
			IncPacket packet = event.getPacket();
		}
	}
}
