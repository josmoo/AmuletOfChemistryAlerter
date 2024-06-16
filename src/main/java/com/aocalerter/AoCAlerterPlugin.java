package com.aocalerter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


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

	/*
	* This was automatically created from the wiki https://oldschool.runescape.wiki/w/List_of_banks
	* if there is an error please let me know
	* */
	private static final List<WorldArea> BANK_AREAS = ImmutableList.of(
			new WorldArea(3179, 3435, 12, 12, 0), // Varrock west bank
			new WorldArea(2940, 3361, 12, 12, 0), // Falador west bank
			new WorldArea(3089, 3488, 12, 12, 0), // Edgeville bank
			new WorldArea(3087, 3238, 12, 12, 0), // Draynor bank
			new WorldArea(3248, 3415, 12, 12, 0), // Varrock east bank
			new WorldArea(3009, 3351, 12, 12, 0), // Falador east bank
			new WorldArea(3263, 3161, 12, 12, 0), // Al Kharid bank
			new WorldArea(2804, 3436, 12, 12, 0), // Catherby bank
			new WorldArea(2377, 4453, 12, 12, 0), // Zanaris bank
			new WorldArea(2719, 3486, 12, 12, 0), // Seers' Village bank
			new WorldArea(2647, 3278, 12, 12, 0), // Ardougne south bank
			new WorldArea(2611, 3327, 12, 12, 0), // Ardougne north bank
			new WorldArea(3116, 3118, 12, 12, 0), // Tutorial Island
			new WorldArea(2607, 3087, 12, 12, 0), // Yanille bank
			new WorldArea(2440, 3418, 12, 12, 0), // Gnome Stronghold south bank
			new WorldArea(2438, 3479, 12, 12, 1), // Gnome Stronghold north bank
			new WorldArea(2846, 2949, 12, 12, 0), // Shilo Village bank
			new WorldArea(3302, 3113, 12, 12, 0), // Shantay Pass
			new WorldArea(2727, 3373, 12, 12, 2), // Legends' Guild bank
			new WorldArea(3084, 3950, 12, 12, 0), // Mage Arena bank
			new WorldArea(2582, 3410, 12, 12, 0), // Fishing Guild bank
			new WorldArea(3378, 3264, 12, 12, 0), // Emir's Arena
			new WorldArea(3506, 3472, 12, 12, 0), // Canifis bank
			new WorldArea(2614, 3889, 12, 12, 0), // Etceteria bank
			new WorldArea(2439, 3076, 12, 12, 0), // Castle Wars
			new WorldArea(3686, 3462, 12, 12, 0), // Port Phasmatys bank
			new WorldArea(2832, 10204, 12, 12, 0), // Keldagrim bank
			new WorldArea(3036, 4966, 12, 12, 0), // Emerald Benedict
			new WorldArea(2346, 3156, 12, 12, 0), // Lletya bank
			new WorldArea(2440, 5173, 12, 12, 0), // Mor Ul Rek north bank
			new WorldArea(3421, 2885, 12, 12, 0), // Nardah bank
			new WorldArea(3675, 2976, 12, 12, 0), // Mos Le'Harmless bank
			new WorldArea(3213, 9617, 12, 12, 0), // Culinaromancer's Chest
			new WorldArea(3490, 3205, 12, 12, 0), // Burgh de Rott bank
			new WorldArea(2661, 2647, 12, 12, 0), // Void Knights' Outpost bank
			new WorldArea(2324, 3684, 12, 12, 0), // Piscatoris bank
			new WorldArea(2837, 3537, 12, 12, 0), // Warriors' Guild bank
			new WorldArea(2095, 3913, 12, 12, 0), // Lunar Isle bank
			new WorldArea(3203, 3213, 12, 12, 2), // Lumbridge Castle bank
			new WorldArea(3306, 2793, 12, 12, 0), // Sophanem bank
			new WorldArea(3189, 4564, 12, 12, 0), // Odovacar
			new WorldArea(2331, 3801, 12, 12, 0), // Neitiznot bank
			new WorldArea(2411, 3795, 12, 12, 0), // Jatizso bank
			new WorldArea(2696, 5344, 12, 12, 0), // Dorgesh-Kaan bank
			new WorldArea(3215, 3213, 12, 12, 0), // PvP world
			new WorldArea(2964, 3336, 12, 12, 0), // PvP world
			new WorldArea(3088, 3463, 12, 12, 0), // PvP world
			new WorldArea(2751, 3472, 12, 12, 0), // PvP world
			new WorldArea(2925, 10189, 12, 12, 0), // Blast Furnace
			new WorldArea(3113, 9692, 12, 12, 0), // Motherlode Mine
			new WorldArea(3159, 3484, 12, 12, 0), // Grand Exchange
			new WorldArea(2930, 3274, 12, 12, 0), // Crafting Guild
			new WorldArea(2531, 3568, 12, 12, 0), // Barbarian Outpost
			new WorldArea(2656, 3155, 12, 12, 0), // Port Khazard
			new WorldArea(3142, 3442, 12, 12, 0), // Cooks' Guild
			new WorldArea(1624, 3739, 12, 12, 0), // Arceuus bank
			new WorldArea(1483, 3858, 12, 12, 0), // Blast mine
			new WorldArea(1711, 3458, 12, 12, 0), // Charcoal furnace
			new WorldArea(1742, 3593, 12, 12, 0), // Hosidius bank
			new WorldArea(1670, 3610, 12, 12, 0), // Hosidius Kitchen
			new WorldArea(1606, 3676, 12, 12, 2), // Kourend Castle bank
			new WorldArea(1520, 3733, 12, 12, 0), // Lovakengj bank
			new WorldArea(1432, 3823, 12, 12, 0), // Lovakengj mine bank
			new WorldArea(1797, 3782, 12, 12, 0), // Port Piscarilius bank
			new WorldArea(1481, 3587, 12, 12, 0), // Shayzien bank
			new WorldArea(1449, 3854, 12, 12, 0), // Sulphur mine
			new WorldArea(1478, 3638, 12, 12, 0), // War Tent
			new WorldArea(1803, 3561, 12, 12, 0), // Vinery
			new WorldArea(2775, 2778, 12, 12, 0), // Ape Atoll bank
			new WorldArea(1585, 3472, 12, 12, 0), // Woodcutting Guild
			new WorldArea(1543, 9869, 12, 12, 0), // Woodcutting Guild
			new WorldArea(1634, 3938, 12, 12, 0), // Wintertodt
			new WorldArea(3010, 5619, 12, 12, 0), // Ourania Altar
			new WorldArea(1507, 3415, 12, 12, 0), // Land's End
			new WorldArea(1248, 3566, 12, 12, 0), // Mount Quidamortem bank
			new WorldArea(2537, 5138, 12, 12, 0), // Mor Ul Rek east bank
			new WorldArea(3006, 9712, 12, 12, 0), // Mining Guild
			new WorldArea(3735, 3798, 12, 12, 0), // Fossil Island bank
			new WorldArea(3766, 3892, 12, 12, 0), // Bank Chest-wreck
			new WorldArea(3813, 3803, 12, 12, 0), // Volcanic Mine
			new WorldArea(2457, 2842, 12, 12, 1), // Myths' Guild
			new WorldArea(2564, 2859, 12, 12, 0), // Corsair Cove bank
			new WorldArea(3645, 3200, 12, 12, 0), // Ver Sinhaza bank
			new WorldArea(1247, 3736, 12, 12, 0), // Farming Guild
			new WorldArea(1243, 3755, 12, 12, 0), // Farming Guild bank
			new WorldArea(1318, 3818, 12, 12, 0), // Mount Karuulm
			new WorldArea(3291, 6054, 12, 12, 0), // Prifddinas south bank
			new WorldArea(3251, 6102, 12, 12, 0), // Prifddinas north bank
			new WorldArea(3596, 3360, 12, 12, 0), // Darkmeyer bank
			new WorldArea(2394, 5977, 12, 12, 0), // Hallowed Sepulchre
			new WorldArea(3124, 3626, 12, 12, 0), // Ferox Enclave
			new WorldArea(3804, 3014, 12, 12, 0), // Trouble Brewing
			new WorldArea(3150, 2831, 12, 12, 0), // Ruins of Unkah
			new WorldArea(2972, 5793, 12, 12, 0), // Ruins of Camdozaal
			new WorldArea(1742, 5470, 12, 12, 0), // Clan Hall
			new WorldArea(2898, 5199, 12, 12, 0), // Ashuelot Reis
			new WorldArea(3613, 9467, 12, 12, 0), // Guardians of the Rift
			new WorldArea(3347, 9114, 12, 12, 0), // Tombs of Amascut (manually edited)
			new WorldArea(3299, 5192, 12, 12, 0), // Chambers of Xeric
			new WorldArea(3420, 4058, 12, 12, 0), // Daimon's Crater
			new WorldArea(1450, 9562, 12, 12, 0), // Cam Torum bank
			new WorldArea(1536, 3034, 12, 12, 0), // Hunter Guild
			new WorldArea(1774, 3090, 12, 12, 0), // Fortis east bank
			new WorldArea(1642, 3112, 12, 12, 0), // Fortis west bank
			new WorldArea(1799, 9495, 12, 12, 0), // Fortis Colosseum
			new WorldArea(3361, 3312, 12, 12, 1) // Mage Training Arena
	);


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
			if(!config.activeNearBank() || nearABank()) {
				clientThread.invokeLater(() -> {
					final ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
					if (containerHasMatch(container, UNF_POTION_TO_SECONDARIES)) {
						checkAoC();
					}
				});
			}
		}
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
		for(WorldArea worldArea: BANK_AREAS)
		{
			if(playerPos.isInArea(worldArea))
			{
				return true;
			}
		}
		return false;
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