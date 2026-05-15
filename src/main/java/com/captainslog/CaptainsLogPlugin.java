package com.captainslog;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.party.PartyService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "Captain's Log",
	description = "Track other players on your boat, etc.",
	tags = {"sailing", "deep", "sea", "trawling", "captains", "log", "crew"}
)

public class CaptainsLogPlugin extends Plugin
{

	private static final String CONFIG_GROUP_KEY = "CaptainsLog";
	@Inject
	private Client client;

	@Inject
	private CaptainsLogConfig config;
	@Inject
	private CaptainsLogPanelOverlay overlay;
	@Inject
	private DebugPanelOverlay debug;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ConfigManager configManager;

	@Inject
	private PartyService partyService;
	@Inject
	private OverlayManager overlayManager;

	@Getter
	private boolean isOnBoat = false;
	@Getter
	private int ticksOffBoat = Integer.MAX_VALUE; // number of ticks player has been off boat

	@Getter
	private List<String> crewList = Collections.emptyList();
	@Getter
	private Map<String, Integer> ticksOnShip = new HashMap<>();
	@Getter
	private Map<String, Integer> ticksOffShip = new HashMap<>();
	public final int RAFT_WORLD_ENTITY_TYPE = 1;
	public final int SKIFF_WORLD_ENTITY_TYPE = 2;
	public final int SLOOP_WORLD_ENTITY_TYPE = 3;
	public Map<Integer, Integer> boats = new HashMap<>();

	@Provides
	CaptainsLogConfig provideConfig(ConfigManager cm)
	{
		return cm.getConfig(CaptainsLogConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		if (shouldShowOverlay())
		{
			overlayManager.add(overlay);
		}

		if (config.showDebugInfo()) {
			overlayManager.add(debug);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlayManager.remove(debug);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP_KEY))
		{
			return;
		}

		if ("showOverlay".equals(event.getKey()))
		{
			if (shouldShowOverlay())
			{
				overlayManager.add(overlay);
			}
			else
			{
				overlayManager.remove(overlay);
			}
		}

		if ("debug".equals(event.getKey()))
		{
			if (config.showDebugInfo())
			{
				overlayManager.add(debug);
			}
			else
			{
				overlayManager.remove(debug);
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		final Player local = client.getLocalPlayer();
		if (local == null)
		{
			crewList = Collections.emptyList();
			return;
		}

		final WorldView rootView = client.getTopLevelWorldView();
		if (rootView == null)
		{
			crewList = Collections.emptyList();
			return;
		}

		if (shouldShowOverlay())
		{
			overlayManager.add(overlay);
		}
		else
		{
			overlayManager.remove(overlay);
		}

		if (!isOnBoat) {
			// prevent overflow
			if (ticksOffBoat < Integer.MAX_VALUE) {
				ticksOffBoat += 1;
			}
		} else {
			ticksOffBoat = 0;
		}

		List<Player> foundCrew = new ArrayList<>();
		// only update crew list when on boat
		if (isOnBoat) {
			findCrew(local.getWorldView(), foundCrew);
			List<String> crewNames = new ArrayList<>();

			for (Player p : foundCrew) {
				if (p == null)
				{
					continue;
				}
				String name = p.getName();
				if (name != null && !name.isEmpty())
				{
					crewNames.add(name);
				}
			}

			for (String name : crewNames)
			{
				ticksOnShip.merge(name, 1, Integer::sum);
			}

			crewList = Collections.unmodifiableList(crewNames);

			for (String crewname : ticksOnShip.keySet()) {
				if (crewList.contains(crewname)) {
					// crew is on board, reset to 0
					ticksOffShip.put(crewname, 0);
				} else {
					ticksOffShip.merge(crewname, 1, Integer::sum);
					// crew is off for x ticks, remove them
					if (ticksOffShip.get(crewname) > config.removeCrewmateTicks()) {
						ticksOnShip.remove(crewname);
					}
				}
			}
		}

		if (!shouldKeepInfo()) {
			if (!ticksOnShip.isEmpty()) {
				ticksOnShip = new HashMap<>();
			}
			if (!ticksOffShip.isEmpty()) {
				ticksOffShip = new HashMap<>();
			}
		}
	}

	static String formatDurationTicks(int ticks)
	{
		long totalMillis = ticks * 600L;
		long totalSeconds = totalMillis / 1000L;
		long totalMinutes = totalSeconds / 60L;
		long minutes = totalMinutes % 60L;
		long hours = totalSeconds / 3600L;
		long seconds = totalSeconds % 60L;
		if (hours == 0) {
			return String.format("%02d:%02d", minutes, seconds);
		} else {
			return String.format("%d:%02d:%02d", hours, minutes, seconds);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged e) {
		int changed = e.getVarbitId();

		switch (changed) {
			case VarbitID.SAILING_PLAYER_IS_ON_PLAYER_BOAT:
				isOnBoat = e.getValue() == 1;
		}
	}

	@Subscribe
	public void onWorldEntitySpawned(WorldEntitySpawned event) {
		WorldEntity entity = event.getWorldEntity();
		WorldEntityConfig cfg = entity.getConfig();

		if (cfg == null) {
			return;
		}

		WorldView view = entity.getWorldView();
		if (view == null) return;
		if (cfg.getId() == RAFT_WORLD_ENTITY_TYPE || cfg.getId() == SKIFF_WORLD_ENTITY_TYPE || cfg.getId() == SLOOP_WORLD_ENTITY_TYPE) {
			boats.put(view.getId(), cfg.getId());
		}
	}
	public void onWorldEntityDespawned(WorldEntityDespawned event)
	{
		WorldEntity entity = event.getWorldEntity();
		WorldEntityConfig cfg = entity.getConfig();

		if (cfg == null) {
			return;
		}

		int worldViewId = entity.getWorldView().getId();

		boats.remove(worldViewId);
	}

	boolean shouldShowOverlay() {
		// shows overlay when
		// 		overlay enabled
		// 		user is on boat or shouldKeepInfo
		return config.showOverlay() && (isOnBoat || shouldKeepInfo());
	}

	boolean shouldKeepInfo() {
		// user left boat within the past x ticks (default 5 min, based on config)
		return ticksOffBoat <= config.removeOverlayTicks();
	}

	private void findCrew(WorldView worldView, List<Player> out)
	{
		for (Player p : worldView.players())
		{
			if (p != null)
			{
				out.add(p);
			}
		}
	}

	Integer getBoatType() {
		final Player localPlayer = client.getLocalPlayer();

		if (localPlayer == null || localPlayer.getWorldView() == null) return -1;
        return boats.getOrDefault(localPlayer.getWorldView().getId(), -1);
	}
}
