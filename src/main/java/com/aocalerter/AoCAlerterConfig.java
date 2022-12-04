package com.aocalerter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(AoCAlerterConfig.CONFIG_GROUP)
public interface AoCAlerterConfig extends Config
{
	String CONFIG_GROUP = "aocalerter";
	@ConfigItem(
		keyName = "useIDList",
		name = "Notify by ID",
		description = "Only notify for a list of IDs",
		position = 1
	)
	default boolean useIDList()
	{
		return false;
	}
	@ConfigItem(
		keyName = "potionsToAlert",
		name = "IDs to notify",
		description = "The IDs of the potions that you would like to be alerted for",
		position = 2
	)
	default String desiredList()
	{
		return "";
	}
}

