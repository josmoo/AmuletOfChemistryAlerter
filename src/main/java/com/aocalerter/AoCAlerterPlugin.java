package com.aocalerter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;


@Slf4j
@PluginDescriptor(name = "Amulet of Chemistry Alerter")
public class AoCAlerterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private AoCAlerterConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Notifier notifier;

	public ArrayList<Integer> desiredIDs = new ArrayList<Integer>();

	public ArrayList<Integer> ignoreIDs = new ArrayList<Integer>();

	private static final Map<Integer, Set<Integer>> UNF_POTION_TO_SECONDARIES = ImmutableMap.<Integer, Set<Integer>>builder()
			.put(ItemID.ANCIENT_BREW1, ImmutableSet.of(ItemID.ANCIENT_ESSENCE))
			.put(ItemID.ANCIENT_BREW2, ImmutableSet.of(ItemID.ANCIENT_ESSENCE))
			.put(ItemID.ANCIENT_BREW3, ImmutableSet.of(ItemID.ANCIENT_ESSENCE))
			.put(ItemID.ANTIDOTE1_5958, ImmutableSet.of(ItemID.ZULRAHS_SCALES))
			.put(ItemID.ANTIDOTE2_5956, ImmutableSet.of(ItemID.ZULRAHS_SCALES))
			.put(ItemID.ANTIDOTE3_5954, ImmutableSet.of(ItemID.ZULRAHS_SCALES))
			.put(ItemID.ANTIFIRE_POTION1, ImmutableSet.of(ItemID.LAVA_SCALE_SHARD))
			.put(ItemID.ANTIFIRE_POTION2, ImmutableSet.of(ItemID.LAVA_SCALE_SHARD))
			.put(ItemID.ANTIFIRE_POTION3, ImmutableSet.of(ItemID.LAVA_SCALE_SHARD))
			.put(ItemID.BASTION_POTION1, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.BASTION_POTION2, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.BASTION_POTION3, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.BATTLEMAGE_POTION1, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.BATTLEMAGE_POTION2, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.BATTLEMAGE_POTION3, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.MAGIC_POTION1, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.MAGIC_POTION2, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.MAGIC_POTION3, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.RANGING_POTION1, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.RANGING_POTION2, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.RANGING_POTION3, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_ANTIFIRE_POTION1, ImmutableSet.of(ItemID.LAVA_SCALE_SHARD))
			.put(ItemID.SUPER_ANTIFIRE_POTION2, ImmutableSet.of(ItemID.LAVA_SCALE_SHARD))
			.put(ItemID.SUPER_ANTIFIRE_POTION3, ImmutableSet.of(ItemID.LAVA_SCALE_SHARD))
			.put(ItemID.SUPER_ATTACK1, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_ATTACK2, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_ATTACK3, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_COMBAT_POTION1, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_COMBAT_POTION2, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_COMBAT_POTION3, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_DEFENCE1, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_DEFENCE2, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_DEFENCE3, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_ENERGY1, ImmutableSet.of(ItemID.AMYLASE_CRYSTAL))
			.put(ItemID.SUPER_ENERGY2, ImmutableSet.of(ItemID.AMYLASE_CRYSTAL))
			.put(ItemID.SUPER_ENERGY3, ImmutableSet.of(ItemID.AMYLASE_CRYSTAL))
			.put(ItemID.SUPER_STRENGTH1, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_STRENGTH2, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))
			.put(ItemID.SUPER_STRENGTH3, ImmutableSet.of(ItemID.CRYSTAL_DUST_23964))

			.put(ItemID.AVANTOE_POTION_UNF, ImmutableSet.of(ItemID.KEBBIT_TEETH_DUST, ItemID.MORT_MYRE_FUNGUS, ItemID.SNAPE_GRASS))
			.put(ItemID.CADANTINE_BLOOD_POTION_UNF, ImmutableSet.of(ItemID.POTATO_CACTUS, ItemID.WINE_OF_ZAMORAK))
			.put(ItemID.CADANTINE_POTION_UNF, ImmutableSet.of(ItemID.POTATO_CACTUS, ItemID.WHITE_BERRIES, ItemID.WINE_OF_ZAMORAK))
			.put(ItemID.DWARF_WEED_POTION_UNF, ImmutableSet.of(ItemID.LILY_OF_THE_SANDS, ItemID.NIHIL_DUST, ItemID.WINE_OF_ZAMORAK))
			.put(ItemID.GUAM_POTION_UNF, ImmutableSet.of(ItemID.EYE_OF_NEWT))
			.put(ItemID.HARRALANDER_POTION_UNF, ImmutableSet.of(ItemID.CHOCOLATE_DUST, ItemID.GOAT_HORN_DUST, ItemID.RED_SPIDERS_EGGS, ItemID.VOLCANIC_ASH))
			.put(ItemID.IRIT_POTION_UNF, ImmutableSet.of(ItemID.EYE_OF_NEWT, ItemID.UNICORN_HORN_DUST))
			.put(ItemID.KWUARM_POTION_UNF, ImmutableSet.of(ItemID.LIMPWURT_ROOT))
			.put(ItemID.LANTADYME_POTION_UNF, ImmutableSet.of(ItemID.DRAGON_SCALE_DUST, ItemID.POTATO_CACTUS))
			.put(ItemID.MARRENTILL_POTION_UNF, ImmutableSet.of(ItemID.UNICORN_HORN_DUST))
			.put(ItemID.RANARR_POTION_UNF, ImmutableSet.of(ItemID.SNAPE_GRASS, ItemID.WHITE_BERRIES))
			.put(ItemID.SNAPDRAGON_POTION_UNF, ImmutableSet.of(ItemID.RED_SPIDERS_EGGS))
			.put(ItemID.TARROMIN_POTION_UNF, ImmutableSet.of(ItemID.ASHES, ItemID.LIMPWURT_ROOT))
			.put(ItemID.TOADFLAX_POTION_UNF, ImmutableSet.of(ItemID.CRUSHED_NEST, ItemID.PHARMAKOS_BERRIES, ItemID.TOADS_LEGS))
			.put(ItemID.TORSTOL_POTION_UNF, ImmutableSet.of(ItemID.JANGERBERRIES))

			.put(ItemID.MIXTURE__STEP_21, ImmutableSet.of(ItemID.NAIL_BEAST_NAILS))
			.put(ItemID.MIXTURE__STEP_22, ImmutableSet.of(ItemID.NAIL_BEAST_NAILS))
			.put(ItemID.MIXTURE__STEP_23, ImmutableSet.of(ItemID.NAIL_BEAST_NAILS))
			.put(ItemID.GUTHIX_BALANCE_UNF_7654, ImmutableSet.of(ItemID.SILVER_DUST))
			.put(ItemID.GUTHIX_BALANCE_UNF_7656, ImmutableSet.of(ItemID.SILVER_DUST))
			.put(ItemID.GUTHIX_BALANCE_UNF_7658, ImmutableSet.of(ItemID.SILVER_DUST))
			.build();



	@Override
	protected void startUp()
	{
		desiredIDs.clear();
		splitIdList(config.desiredList(), desiredIDs);
		ignoreIDs.clear();
		splitIdList(config.ignoreList(), ignoreIDs);
		clientThread.invokeLater(() -> {
			final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
			if (containerHasMatch(inventory, UNF_POTION_TO_SECONDARIES))
			{
				checkAoC();
			}
		});


	}


	@Subscribe
	void onItemContainerChanged(ItemContainerChanged event)
	{
		final int changedContainerId = event.getContainerId();

		//we only care about changes to inventory and equipment
		if (changedContainerId == InventoryID.INVENTORY.getId() || changedContainerId == InventoryID.EQUIPMENT.getId()) {
			if(!config.activeNearBank() || nearBank()) {
				clientThread.invokeLater(() -> {
					final ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
					if (containerHasMatch(container, UNF_POTION_TO_SECONDARIES)) {
						checkAoC();
					}
				});
			}
		}
	}

	private boolean nearBank()
	{
		WorldView worldView = client.getTopLevelWorldView();
		Tile[][] currentPlane = worldView.getScene().getTiles()[worldView.getPlane()];
		return (worldView.npcs().stream().map(NPC::getName).filter(Objects::nonNull).map(String::toLowerCase)
				.anyMatch(name-> name.contains("banker"))
				|| Arrays.stream(currentPlane).flatMap(Arrays::stream).filter(Objects::nonNull)
				.map(Tile::getGameObjects).flatMap(Arrays::stream).filter(Objects::nonNull).map(TileObject::getId)
				.map(id -> client.getObjectDefinition(id)).map(ObjectComposition::getName).map(String::toLowerCase)
				.anyMatch(name -> name.startsWith("bank")));
	}

	private void checkAoC()
	{
		ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (itemContainer == null || itemContainer.getItems()[EquipmentInventorySlot.AMULET.getSlotIdx()].getId() != 21163)
		{
			notifier.notify("You don't have an Amulet of Chemistry equipped!");
		}
	}

	//from:https://github.com/MoreBuchus/buchus-plugins/blob/61c894cc8ee0214920d6bdbaf5750190820a290c/src/main/java/com/betternpchighlight/BetterNpcHighlightPlugin.java#L199
	private void splitIdList(String configStr, ArrayList<Integer> idList)
	{
		if (!configStr.equals(""))
		{
			for (String str : configStr.split(","))
			{
				if (!str.trim().equals(""))
				{
					try
					{
						idList.add(Integer.parseInt(str.trim()));
					}
					catch (Exception ex)
					{
						log.info("AoC Alerter: " + ex.getMessage());
					}
				}
			}
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		desiredIDs.clear();
		ignoreIDs.clear();
	}

	//check the container for a matching pair of unfinished potions and secondaries
	private boolean containerHasMatch(@Nullable final ItemContainer container, final Map<Integer, Set<Integer>> unfToSecondaries)
	{
		if (container == null)
		{
			return false;
		}

		if(!config.useIDList()) //if the user doesn't specify a list of IDs to check,
		{
			if(!config.useIgnoreList())// and doesn't want to ignore any IDs, then we'll check all unf potions
			{
				for (Map.Entry<Integer, Set<Integer>> entry : unfToSecondaries.entrySet()) //for every unfinished potion
				{
					if (container.contains(entry.getKey())) //if we have the unfinished potion
					{
						for (int secondary : entry.getValue())//for every secondary of that unfinished potion
						{
							if (container.contains(secondary)) //if we have that secondary
							{
								return true;
							}
						}
					}
				}
				return false;
			}
			//otherwise the user doesn't specify a desired list, but does specify an ignore list
			for(Map.Entry<Integer, Set<Integer>> entry : unfToSecondaries.entrySet()) //for every unfinished potion
			{
				for(int ignoreID : ignoreIDs) //for all the IDs the user wants ignored
					{
					if( entry.getKey() != ignoreID) //if the potion isn't one to be ignored
					{
						if (container.contains(entry.getKey())) //if we have the unfinished potion
						{
							for (int secondary : entry.getValue())//for every secondary of that unfinished potion
							{
								if (container.contains(secondary)) //if we have that secondary
								{
									return true;
								}
							}
						}
					}
				}
			return false;
			}
		}
		//otherwise the user does have a desired list,
		if(!config.useIgnoreList())// and doesn't want to ignore any IDs, then we'll check all unf potions
		{
			for (int desiredID : desiredIDs) //for each ID they want checked
			{
				if (container.contains(desiredID)) //if the ID is in the inventory
				{
					for (int secondary : unfToSecondaries.get(desiredID)) //for each secondary of that ID
					{
						if (container.contains(secondary)) //if we have that secondary
						{
							return true;
						}
					}
				}
			}
			return false;
		}
		//otherwise the user does have an ignore list
		for (int desiredID : desiredIDs) //for each ID they want checked
		{
			for (int ignoreID : ignoreIDs)//for all the IDs the user wants ignored
			{
				if (desiredID != ignoreID)//if the potion isn't one to be ignored
				{
					if (container.contains(desiredID)) //if the ID is in the inventory
					{
						for (int secondary : unfToSecondaries.get(desiredID)) //for each secondary of that ID
						{
							if (container.contains(secondary)) //if we have that secondary
							{
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public boolean nearABank(){
		WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();
		return true;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(config.CONFIG_GROUP))
		{
			desiredIDs.clear();
			splitIdList(config.desiredList(), desiredIDs);
			ignoreIDs.clear();
			splitIdList(config.ignoreList(), ignoreIDs);
		}
	}

	@Provides
	AoCAlerterConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AoCAlerterConfig.class);
	}
}