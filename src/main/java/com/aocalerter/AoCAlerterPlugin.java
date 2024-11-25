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

	/*
	* This was automatically created from the wiki https://oldschool.runescape.wiki/w/List_of_banks
	* if there is an error please let me know
	* */
	private static final List<WorldArea> BANK_AREAS = ImmutableList.of(
			new WorldArea(1240, 3753, 12, 12, 0), // Farming Guild bank
			new WorldArea(1246, 3564, 12, 12, 0), // Mount Quidamortem bank
			new WorldArea(1247, 3736, 12, 12, 0), // Farming Guild#Central area|Farming Guild chest
			new WorldArea(1318, 3819, 12, 12, 0), // Mount Karuulm#Bank|Mount Karuulm chest
			new WorldArea(1376, 2860, 12, 12, 0), // Mistrock bank
			new WorldArea(1393, 2921, 12, 12, 0), // Aldarin bank
			new WorldArea(1432, 3823, 12, 12, 0), // Lovakengj mine bank
			new WorldArea(1449, 3854, 12, 12, 0), // Sulphur mine#Bank chest|Sulphur mine chest
			new WorldArea(1450, 9562, 12, 12, 0), // Cam Torum bank
			new WorldArea(1478, 3638, 12, 12, 0), // War Tent#Bank chests|War Tent chests
			new WorldArea(1479, 3640, 12, 12, 0), // Shayzien Encampment#Bank|Shayzien Encampment bank chest
			new WorldArea(1481, 3587, 12, 12, 0), // Shayzien bank
			new WorldArea(1483, 3858, 12, 12, 0), // Blast mine#Bank chests|Blast mine chests
			new WorldArea(1507, 3415, 12, 12, 0), // Land's End#Bank chest|Land's End chest
			new WorldArea(1514, 3223, 12, 12, 0), // Quetzacalli Gorge bank
			new WorldArea(1520, 3287, 12, 12, 0), // The Hueycoatl|The Darkfrost bank
			new WorldArea(1520, 3733, 12, 12, 0), // Lovakengj bank
			new WorldArea(1536, 3034, 12, 12, 0), // Hunter Guild#Bank|Hunter Guild bank
			new WorldArea(1543, 9869, 12, 12, 0), // Woodcutting Guild#Bank chest 2|Ent dungeon chest
			new WorldArea(1583, 3469, 12, 12, 0), // Woodcutting Guild#Bank chest|Woodcutting Guild chest
			new WorldArea(1606, 3676, 12, 12, 2), // Kourend Castle bank
			new WorldArea(1624, 3739, 12, 12, 0), // Arceuus bank
			new WorldArea(1632, 3936, 12, 12, 0), // Wintertodt#Bank chest|Wintertodt chest
			new WorldArea(1642, 3112, 12, 12, 0), // Fortis west bank
			new WorldArea(1670, 3610, 12, 12, 0), // Hosidius Kitchen#Layout|Hosidius Kitchen chest
			new WorldArea(1711, 3458, 12, 12, 0), // Charcoal furnace#Bank chest|Charcoal camp chest
			new WorldArea(1742, 5470, 12, 12, 0), // Clan Hall#Features|Clan Hall bank
			new WorldArea(1742, 3593, 12, 12, 0), // Hosidius bank
			new WorldArea(1774, 3090, 12, 12, 0), // Fortis east bank
			new WorldArea(1797, 3782, 12, 12, 0), // Port Piscarilius bank
			new WorldArea(1799, 9495, 12, 12, 0), // Fortis Colosseum#Lobby area|Fortis Colosseum chest
			new WorldArea(1803, 3561, 12, 12, 0), // Vinery#Bank chest|Vinery chest
			new WorldArea(2095, 3913, 12, 12, 0), // Lunar Isle bank
			new WorldArea(2324, 3684, 12, 12, 0), // Piscatoris bank
			new WorldArea(2331, 3801, 12, 12, 0), // Neitiznot bank
			new WorldArea(2346, 3156, 12, 12, 0), // Lletya bank
			new WorldArea(2377, 4453, 12, 12, 0), // Zanaris bank
			new WorldArea(2394, 5977, 12, 12, 0), // Hallowed Sepulchre
			new WorldArea(2411, 3795, 12, 12, 0), // Jatizso bank
			new WorldArea(2438, 3479, 12, 12, 1), // Gnome Stronghold north bank
			new WorldArea(2439, 3076, 12, 12, 0), // Castle Wars#Main lobby|Castle Wars lobby chest
			new WorldArea(2440, 5173, 12, 12, 0), // Mor Ul Rek north bank|Tzhaar City bank
			new WorldArea(2440, 3418, 12, 12, 0), // Gnome Stronghold south bank
			new WorldArea(2457, 2842, 12, 12, 1), // Myths' Guild#First floor|Myths' Guild chest
			new WorldArea(2531, 3568, 12, 12, 0), // Barbarian Outpost#Bank chest|Barbarian Outpost chest
			new WorldArea(2537, 5138, 12, 12, 0), // Mor Ul Rek east bank
			new WorldArea(2562, 2857, 12, 12, 0), // Corsair Cove bank
			new WorldArea(2582, 3410, 12, 12, 0), // Fishing Guild bank
			new WorldArea(2607, 3087, 12, 12, 0), // Yanille bank
			new WorldArea(2611, 3327, 12, 12, 0), // Ardougne north bank
			new WorldArea(2614, 3889, 12, 12, 0), // Etceteria bank
			new WorldArea(2647, 3278, 12, 12, 0), // Ardougne south bank
			new WorldArea(2656, 3155, 12, 12, 0), // Port Khazard#Bank chest|Port Khazard chest
			new WorldArea(2661, 2647, 12, 12, 0), // Void Knights' Outpost bank
			new WorldArea(2696, 5344, 12, 12, 0), // Dorgesh-Kaan bank
			new WorldArea(2719, 3486, 12, 12, 0), // Seers' Village bank|Seersâ€™ Village bank
			new WorldArea(2727, 3373, 12, 12, 2), // Legends' Guild bank
			new WorldArea(2751, 3472, 12, 12, 0), // PvP world#Bank chests|Camelot PvP chest
			new WorldArea(2775, 2778, 12, 12, 0), // Ape Atoll bank
			new WorldArea(2804, 3436, 12, 12, 0), // Catherby bank
			new WorldArea(2832, 10204, 12, 12, 0), // Keldagrim bank
			new WorldArea(2837, 3537, 12, 12, 0), // Warriors' Guild bank|Warriorsâ€™ Guild bank
			new WorldArea(2846, 2949, 12, 12, 0), // Shilo Village bank
			new WorldArea(2898, 5199, 12, 12, 0), // Ashuelot Reis|Ancient Prison bank
			new WorldArea(2925, 10189, 12, 12, 0), // Blast Furnace#Bank chest|Blast Furnace bank
			new WorldArea(2930, 3274, 12, 12, 0), // Crafting Guild#Bank chest|Crafting Guild chest
			new WorldArea(2940, 3361, 12, 12, 0), // Falador west bank
			new WorldArea(2964, 3336, 12, 12, 0), // PvP world#Bank chests|Falador PvP chest
			new WorldArea(2972, 5793, 12, 12, 0), // Ruins of Camdozaal#Bank|Camdozaal chest
			new WorldArea(3006, 9712, 12, 12, 0), // Mining Guild#Bank chest|Mining Guild chest
			new WorldArea(3009, 3351, 12, 12, 0), // Falador east bank
			new WorldArea(3010, 5619, 12, 12, 0), // Ourania Altar|Ourania bank
			new WorldArea(3036, 4966, 12, 12, 0), // Emerald Benedict|Emerald Benedict's bank
			new WorldArea(3084, 3950, 12, 12, 0), // Mage Arena bank
			new WorldArea(3087, 3238, 12, 12, 0), // Draynor bank
			new WorldArea(3088, 3463, 12, 12, 0), // PvP world#Bank chests|Edgeville PvP chest
			new WorldArea(3089, 3488, 12, 12, 0), // Edgeville bank
			new WorldArea(3090, 3021, 12, 12, 0), // The Node
			new WorldArea(3113, 9692, 12, 12, 0), // Motherlode Mine#Bank chest|Motherload Mine chest
			new WorldArea(3116, 3118, 12, 12, 0), // Tutorial Island bank
			new WorldArea(3124, 3626, 12, 12, 0), // Ferox Enclave#Bank|Ferox Enclave bank
			new WorldArea(3142, 3442, 12, 12, 0), // Cooks' Guild#Bank|Cook's Guild bank
			new WorldArea(3150, 2831, 12, 12, 0), // Ruins of Unkah#Bank|Unkah chest
			new WorldArea(3159, 3484, 12, 12, 0), // Grand Exchange
			new WorldArea(3179, 3435, 12, 12, 0), // Varrock west bank
			new WorldArea(3189, 4564, 12, 12, 0), // Odovacar|Tarn's Lair bank
			new WorldArea(3203, 3213, 12, 12, 2), // Lumbridge Castle bank
			new WorldArea(3213, 9617, 12, 12, 0), // Culinaromancer's Chest|Culinaromancerâ€™s Chest
			new WorldArea(3215, 3213, 12, 12, 0), // PvP world#Bank chests|Lumbridge PvP chest
			new WorldArea(3248, 3415, 12, 12, 0), // Varrock east bank
			new WorldArea(3251, 6102, 12, 12, 0), // Prifddinas north bank
			new WorldArea(3263, 3161, 12, 12, 0), // Al Kharid bank
			new WorldArea(3272, 5195, 12, 12, 0), // Chambers of Xeric|Chambers of Xeric lobby chest
			new WorldArea(3291, 6054, 12, 12, 0), // Prifddinas south bank
			new WorldArea(3297, 5193, 12, 12, 0), // Chambers of Xeric|Chambers of Xeric lobby chest2
			new WorldArea(3302, 3113, 12, 12, 0), // Shantay Pass#Bank|Shantay Pass bank
			new WorldArea(3306, 2793, 12, 12, 0), // Sophanem bank
			new WorldArea(3327, 5189, 12, 12, 0), // Chambers of Xeric|Chambers of Xeric lobby chest3
			new WorldArea(3339, 2719, 12, 12, 0), // Tombs of Amascut#Lobby|Tombs of Amascut bank
			new WorldArea(3361, 3312, 12, 12, 1), // Mage Training Arena|Mage Training Arena chest
			new WorldArea(3378, 3264, 12, 12, 0), // Emir's Arena#Bank|Emir's Arena bank
			new WorldArea(3420, 4058, 12, 12, 0), // Daimon's Crater#Bank chests|Daimon's Crater chests
			new WorldArea(3421, 2885, 12, 12, 0), // Nardah bank
			new WorldArea(3490, 3205, 12, 12, 0), // Burgh de Rott bank
			new WorldArea(3506, 3472, 12, 12, 0), // Canifis bank
			new WorldArea(3596, 3360, 12, 12, 0), // Darkmeyer bank
			new WorldArea(3613, 9467, 12, 12, 0), // Guardians of the Rift#Bank chest|Guardians of the Rift chest
			new WorldArea(3642, 3198, 12, 12, 0), // Ver Sinhaza bank
			new WorldArea(3675, 2976, 12, 12, 0), // Mos Le'Harmless bank|Mos Leâ€™Harmless bank
			new WorldArea(3686, 3462, 12, 12, 0), // Port Phasmatys bank
			new WorldArea(3733, 3796, 12, 12, 0), // Fossil Island bank
			new WorldArea(3766, 3892, 12, 12, 0), // Bank Chest-wreck
			new WorldArea(3804, 3014, 12, 12, 0), // Trouble Brewing#Bank chest|Trouble Brewing chest
			new WorldArea(3813, 3803, 12, 12, 0)  // Volcanic Mine#Bank chest|Volcanic Mine chest
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
			.put(ItemID.HARRALANDER_POTION_UNF, ImmutableSet.of(ItemID.CHOCOLATE_DUST, ItemID.GOAT_HORN_DUST, ItemID.RED_SPIDERS_EGGS, ItemID.VOLCANIC_ASH, ItemID.ALDARIUM))
			.put(ItemID.IRIT_POTION_UNF, ImmutableSet.of(ItemID.EYE_OF_NEWT, ItemID.UNICORN_HORN_DUST))
			.put(ItemID.KWUARM_POTION_UNF, ImmutableSet.of(ItemID.LIMPWURT_ROOT))
			.put(ItemID.LANTADYME_POTION_UNF, ImmutableSet.of(ItemID.DRAGON_SCALE_DUST, ItemID.POTATO_CACTUS))
			.put(ItemID.MARRENTILL_POTION_UNF, ImmutableSet.of(ItemID.UNICORN_HORN_DUST))
			.put(ItemID.RANARR_POTION_UNF, ImmutableSet.of(ItemID.SNAPE_GRASS, ItemID.WHITE_BERRIES))
			.put(ItemID.SNAPDRAGON_POTION_UNF, ImmutableSet.of(ItemID.RED_SPIDERS_EGGS))
			.put(ItemID.TARROMIN_POTION_UNF, ImmutableSet.of(ItemID.ASHES, ItemID.LIMPWURT_ROOT))
			.put(ItemID.TOADFLAX_POTION_UNF, ImmutableSet.of(ItemID.CRUSHED_NEST, ItemID.PHARMAKOS_BERRIES, ItemID.TOADS_LEGS))
			.put(ItemID.TORSTOL_POTION_UNF, ImmutableSet.of(ItemID.JANGERBERRIES))
			.put(ItemID.HUASCA_POTION_UNF, ImmutableSet.of(ItemID.ALDARIUM))

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
			if (containerHasMatch(inventory))
			{
				checkAoC();
			}
		});
	}

	@Subscribe
	void onItemContainerChanged(ItemContainerChanged event)
	{
		final int changedContainerId = event.getContainerId();

		if (changedContainerId != InventoryID.INVENTORY.getId() && changedContainerId != InventoryID.EQUIPMENT.getId()) {
			return;
		}

		if(config.activeNearBank() && !nearABank()) {
			return;
		}

		clientThread.invokeLater(() -> {
			final ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
			if (containerHasMatch(container)) {
				checkAoC();
			}
		});
	}

	private void checkAoC()
	{
		ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (itemContainer == null)
		{
			notifier.notify("You don't have an Amulet of Chemistry/Alchemy equipped!");
			return;
		}

		int amuletId = itemContainer.getItems()[EquipmentInventorySlot.AMULET.getSlotIdx()].getId();
		if(amuletId != 21163 && amuletId != ItemID.ALCHEMISTS_AMULET_29990) {
			notifier.notify("You don't have an Amulet of Chemistry/Alchemy equipped!");
		}
	}

	//from:https://github.com/MoreBuchus/buchus-plugins/blob/61c894cc8ee0214920d6bdbaf5750190820a290c/src/main/java/com/betternpchighlight/BetterNpcHighlightPlugin.java#L199
	private void splitIdList(String configStr, ArrayList<Integer> idList)
	{
		if (configStr.equals(""))
		{
			return;
		}

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

	@Override
	protected void shutDown() throws Exception
	{
		desiredIDs.clear();
		ignoreIDs.clear();
	}

	private boolean containerHasMatch(@Nullable final ItemContainer container)
	{
		if (container == null)
		{
			return false;
		}

		return Arrays.stream(container.getItems()).map(Item::getId).filter(id ->
				(!config.useIgnoreList() || !ignoreIDs.contains(id))
				&& (!config.useIDList() || desiredIDs.contains(id))
				&& UNF_POTION_TO_SECONDARIES.containsKey(id))
				.map(UNF_POTION_TO_SECONDARIES::get).flatMap(Collection::stream).anyMatch(container::contains);
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