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
			new WorldArea(1246, 3759, 12, 12, 0), // Farming Guild bank
			new WorldArea(1252, 3570, 12, 12, 0), // Mount Quidamortem bank
			new WorldArea(1253, 3742, 12, 12, 0), // Farming Guild#Central area|Farming Guild chest
			new WorldArea(1324, 3825, 12, 12, 0), // Mount Karuulm#Bank|Mount Karuulm chest
			new WorldArea(1382, 2866, 12, 12, 0), // Mistrock bank
			new WorldArea(1399, 2927, 12, 12, 0), // Aldarin bank
			new WorldArea(1438, 3829, 12, 12, 0), // Lovakengj mine bank
			new WorldArea(1455, 3860, 12, 12, 0), // Sulphur mine#Bank chest|Sulphur mine chest
			new WorldArea(1456, 9568, 12, 12, 0), // Cam Torum bank
			new WorldArea(1484, 3644, 12, 12, 0), // War Tent#Bank chests|War Tent chests
			new WorldArea(1485, 3646, 12, 12, 0), // Shayzien Encampment#Bank|Shayzien Encampment bank chest
			new WorldArea(1487, 3593, 12, 12, 0), // Shayzien bank
			new WorldArea(1489, 3864, 12, 12, 0), // Blast mine#Bank chests|Blast mine chests
			new WorldArea(1513, 3421, 12, 12, 0), // Land's End#Bank chest|Land's End chest
			new WorldArea(1520, 3229, 12, 12, 0), // Quetzacalli Gorge bank
			new WorldArea(1526, 3293, 12, 12, 0), // The Hueycoatl|The Darkfrost bank
			new WorldArea(1526, 3739, 12, 12, 0), // Lovakengj bank
			new WorldArea(1542, 3040, 12, 12, 0), // Hunter Guild#Bank|Hunter Guild bank
			new WorldArea(1549, 9875, 12, 12, 0), // Woodcutting Guild#Bank chest 2|Ent dungeon chest
			new WorldArea(1589, 3475, 12, 12, 0), // Woodcutting Guild#Bank chest|Woodcutting Guild chest
			new WorldArea(1612, 3682, 12, 12, 2), // Kourend Castle bank
			new WorldArea(1630, 3745, 12, 12, 0), // Arceuus bank
			new WorldArea(1638, 3942, 12, 12, 0), // Wintertodt#Bank chest|Wintertodt chest
			new WorldArea(1648, 3118, 12, 12, 0), // Fortis west bank
			new WorldArea(1676, 3616, 12, 12, 0), // Hosidius Kitchen#Layout|Hosidius Kitchen chest
			new WorldArea(1717, 3464, 12, 12, 0), // Charcoal furnace#Bank chest|Charcoal camp chest
			new WorldArea(1748, 5476, 12, 12, 0), // Clan Hall#Features|Clan Hall bank
			new WorldArea(1748, 3599, 12, 12, 0), // Hosidius bank
			new WorldArea(1780, 3096, 12, 12, 0), // Fortis east bank
			new WorldArea(1803, 3788, 12, 12, 0), // Port Piscarilius bank
			new WorldArea(1805, 9501, 12, 12, 0), // Fortis Colosseum#Lobby area|Fortis Colosseum chest
			new WorldArea(1809, 3567, 12, 12, 0), // Vinery#Bank chest|Vinery chest
			new WorldArea(2101, 3919, 12, 12, 0), // Lunar Isle bank
			new WorldArea(2330, 3690, 12, 12, 0), // Piscatoris bank
			new WorldArea(2337, 3807, 12, 12, 0), // Neitiznot bank
			new WorldArea(2352, 3162, 12, 12, 0), // Lletya bank
			new WorldArea(2383, 4459, 12, 12, 0), // Zanaris bank
			new WorldArea(2400, 5983, 12, 12, 0), // Hallowed Sepulchre
			new WorldArea(2417, 3801, 12, 12, 0), // Jatizso bank
			new WorldArea(2444, 3485, 12, 12, 1), // Gnome Stronghold north bank
			new WorldArea(2445, 3082, 12, 12, 0), // Castle Wars#Main lobby|Castle Wars lobby chest
			new WorldArea(2446, 5179, 12, 12, 0), // Mor Ul Rek north bank|Tzhaar City bank
			new WorldArea(2446, 3424, 12, 12, 0), // Gnome Stronghold south bank
			new WorldArea(2463, 2848, 12, 12, 1), // Myths' Guild#First floor|Myths' Guild chest
			new WorldArea(2537, 3574, 12, 12, 0), // Barbarian Outpost#Bank chest|Barbarian Outpost chest
			new WorldArea(2543, 5144, 12, 12, 0), // Mor Ul Rek east bank
			new WorldArea(2568, 2863, 12, 12, 0), // Corsair Cove bank
			new WorldArea(2588, 3416, 12, 12, 0), // Fishing Guild bank
			new WorldArea(2613, 3093, 12, 12, 0), // Yanille bank
			new WorldArea(2617, 3333, 12, 12, 0), // Ardougne north bank
			new WorldArea(2620, 3895, 12, 12, 0), // Etceteria bank
			new WorldArea(2653, 3284, 12, 12, 0), // Ardougne south bank
			new WorldArea(2662, 3161, 12, 12, 0), // Port Khazard#Bank chest|Port Khazard chest
			new WorldArea(2667, 2653, 12, 12, 0), // Void Knights' Outpost bank
			new WorldArea(2702, 5350, 12, 12, 0), // Dorgesh-Kaan bank
			new WorldArea(2725, 3492, 12, 12, 0), // Seers' Village bank|Seersâ€™ Village bank
			new WorldArea(2733, 3379, 12, 12, 2), // Legends' Guild bank
			new WorldArea(2757, 3478, 12, 12, 0), // PvP world#Bank chests|Camelot PvP chest
			new WorldArea(2781, 2784, 12, 12, 0), // Ape Atoll bank
			new WorldArea(2810, 3442, 12, 12, 0), // Catherby bank
			new WorldArea(2838, 10210, 12, 12, 0), // Keldagrim bank
			new WorldArea(2843, 3543, 12, 12, 0), // Warriors' Guild bank|Warriorsâ€™ Guild bank
			new WorldArea(2852, 2955, 12, 12, 0), // Shilo Village bank
			new WorldArea(2904, 5205, 12, 12, 0), // Ashuelot Reis|Ancient Prison bank
			new WorldArea(2931, 10195, 12, 12, 0), // Blast Furnace#Bank chest|Blast Furnace bank
			new WorldArea(2936, 3280, 12, 12, 0), // Crafting Guild#Bank chest|Crafting Guild chest
			new WorldArea(2946, 3367, 12, 12, 0), // Falador west bank
			new WorldArea(2970, 3342, 12, 12, 0), // PvP world#Bank chests|Falador PvP chest
			new WorldArea(2978, 5799, 12, 12, 0), // Ruins of Camdozaal#Bank|Camdozaal chest
			new WorldArea(3012, 9718, 12, 12, 0), // Mining Guild#Bank chest|Mining Guild chest
			new WorldArea(3015, 3357, 12, 12, 0), // Falador east bank
			new WorldArea(3016, 5625, 12, 12, 0), // Ourania Altar|Ourania bank
			new WorldArea(3042, 4972, 12, 12, 0), // Emerald Benedict|Emerald Benedict's bank
			new WorldArea(3090, 3956, 12, 12, 0), // Mage Arena bank
			new WorldArea(3093, 3244, 12, 12, 0), // Draynor bank
			new WorldArea(3094, 3469, 12, 12, 0), // PvP world#Bank chests|Edgeville PvP chest
			new WorldArea(3095, 3494, 12, 12, 0), // Edgeville bank
			new WorldArea(3096, 3027, 12, 12, 0), // The Node
			new WorldArea(3119, 9698, 12, 12, 0), // Motherlode Mine#Bank chest|Motherload Mine chest
			new WorldArea(3122, 3124, 12, 12, 0), // Tutorial Island bank
			new WorldArea(3130, 3632, 12, 12, 0), // Ferox Enclave#Bank|Ferox Enclave bank
			new WorldArea(3148, 3448, 12, 12, 0), // Cooks' Guild#Bank|Cook's Guild bank
			new WorldArea(3156, 2837, 12, 12, 0), // Ruins of Unkah#Bank|Unkah chest
			new WorldArea(3165, 3490, 12, 12, 0), // Grand Exchange
			new WorldArea(3185, 3441, 12, 12, 0), // Varrock west bank
			new WorldArea(3195, 4570, 12, 12, 0), // Odovacar|Tarn's Lair bank
			new WorldArea(3209, 3219, 12, 12, 2), // Lumbridge Castle bank
			new WorldArea(3219, 9623, 12, 12, 0), // Culinaromancer's Chest|Culinaromancerâ€™s Chest
			new WorldArea(3221, 3219, 12, 12, 0), // PvP world#Bank chests|Lumbridge PvP chest
			new WorldArea(3254, 3421, 12, 12, 0), // Varrock east bank
			new WorldArea(3257, 6108, 12, 12, 0), // Prifddinas north bank
			new WorldArea(3269, 3167, 12, 12, 0), // Al Kharid bank
			new WorldArea(3278, 5201, 12, 12, 0), // Chambers of Xeric|Chambers of Xeric lobby chest
			new WorldArea(3297, 6060, 12, 12, 0), // Prifddinas south bank
			new WorldArea(3303, 5199, 12, 12, 0), // Chambers of Xeric|Chambers of Xeric lobby chest2
			new WorldArea(3308, 3119, 12, 12, 0), // Shantay Pass#Bank|Shantay Pass bank
			new WorldArea(3312, 2799, 12, 12, 0), // Sophanem bank
			new WorldArea(3333, 5195, 12, 12, 0), // Chambers of Xeric|Chambers of Xeric lobby chest3
			new WorldArea(3345, 2725, 12, 12, 0), // Tombs of Amascut#Lobby|Tombs of Amascut bank
			new WorldArea(3367, 3318, 12, 12, 1), // Mage Training Arena|Mage Training Arena chest
			new WorldArea(3384, 3270, 12, 12, 0), // Emir's Arena#Bank|Emir's Arena bank
			new WorldArea(3426, 4064, 12, 12, 0), // Daimon's Crater#Bank chests|Daimon's Crater chests
			new WorldArea(3427, 2891, 12, 12, 0), // Nardah bank
			new WorldArea(3496, 3211, 12, 12, 0), // Burgh de Rott bank
			new WorldArea(3512, 3478, 12, 12, 0), // Canifis bank
			new WorldArea(3602, 3366, 12, 12, 0), // Darkmeyer bank
			new WorldArea(3619, 9473, 12, 12, 0), // Guardians of the Rift#Bank chest|Guardians of the Rift chest
			new WorldArea(3648, 3204, 12, 12, 0), // Ver Sinhaza bank
			new WorldArea(3681, 2982, 12, 12, 0), // Mos Le'Harmless bank|Mos Leâ€™Harmless bank
			new WorldArea(3692, 3468, 12, 12, 0), // Port Phasmatys bank
			new WorldArea(3739, 3802, 12, 12, 0), // Fossil Island bank
			new WorldArea(3772, 3898, 12, 12, 0), // Bank Chest-wreck
			new WorldArea(3810, 3020, 12, 12, 0), // Trouble Brewing#Bank chest|Trouble Brewing chest
			new WorldArea(3819, 3809, 12, 12, 0) // Volcanic Mine#Bank chest|Volcanic Mine chest
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
		if (itemContainer == null || itemContainer.getItems()[EquipmentInventorySlot.AMULET.getSlotIdx()].getId() != 21163)
		{
			notifier.notify("You don't have an Amulet of Chemistry equipped!");
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