package com.aocalerter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("aocalerter")
public interface AoCAlerterConfig extends Config
{
	@ConfigItem(
		keyName = "aocChargeCounter",
		name = "Show remaining charges",
		description = "Show remaining charges of amulet of chemistry",
		position = 1
	)
	default boolean aocChargeCounter()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notify",
		name = "Notify user",
		description = "Notify user when matching unfinished potions to secondaries are in inventory",
		position = 2
	)
	default boolean alert()
	{
		return true;
	}

	@ConfigItem(
		keyName = "potionsToAlert",
		name = "Potions to Alert For",
		description = "Enter the names of the potions that you would like to be alerted for when attempting to amke them without an AoC",
		position = 3
	)
	default String desiredList()
	{
		return "";
	}
}

