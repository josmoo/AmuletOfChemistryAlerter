package com.aocalerter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.Notifier;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


@Slf4j
@PluginDescriptor(name = "Amulet of Chemistry Alerter")
public class AoCAlerterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private AoCAlerterConfig config;

	//@Inject
	//private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Notifier notifier;

	//private classtype counter this needs to be done at some point TODO todo
//todo i just finished htis map so now we need to write a function that chekcs neck for aoc, theninventory for the keys
// , and for each found key,
	//check inventory for values. then alert
	private static final Map<Integer, Set<Integer>> UNF_POTION_TO_SECONDARIES = ImmutableMap.<Integer, Set<Integer>>builder()
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
	protected void startUp() throws Exception
	{
		clientThread.invokeLater(() -> {
			final ItemContainer container = client.getItemContainer(InventoryID.EQUIPMENT);
			if (container != null)
			{
				checkNeck(container.getItems());
			}
		});
	}

	private void checkNeck(final Item[] items)
	{
		if (items[EquipmentInventorySlot.AMULET.getSlotIdx()].getId() == 21163)
		{
			return;
		} //if there is an AoC equipped
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe//this isnt needed
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	private void checkForUnfinishedPotions()
	{
		//boolean hasUnfinishedPotions = containerHasAnyId(client.getItemContainer(InventoryID.INVENTORY), UNF_POTION_IDS);
	}


	//check the container for a matching pair of unfinished potions and secondaries
	private static boolean containerHasMatch(@Nullable final ItemContainer container, final Map<Integer, Set<Integer>> unfToSecondaries)
	{
		if (container == null)
		{
			return false;
		}

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

	@Provides
	AoCAlerterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AoCAlerterConfig.class);
	}
}
