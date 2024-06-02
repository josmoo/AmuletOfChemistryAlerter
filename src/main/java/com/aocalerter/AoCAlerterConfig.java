package com.aocalerter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(AoCAlerterConfig.CONFIG_GROUP)
public interface AoCAlerterConfig extends Config
{
	String CONFIG_GROUP = "aocalerter";

	@ConfigItem(
			keyName = "activeNearBank",
			name = "Only active near bank",
			description = "Only alert when a bank chest or bank teller is rendered",
			position = 1
	)
	default boolean activeNearBank()
	{
		return true;
	}
	@ConfigItem(
		keyName = "useIDList",
		name = "Notify by ID",
		description = "Only notify for a list of IDs",
		position = 2
	)
	default boolean useIDList()
	{
		return false;
	}
	@ConfigItem(
		keyName = "potionsToAlert",
		name = "IDs to notify",
		description = "The IDs of the potions that you would like to be alerted for",
		position = 3
	)
	default String desiredList()
	{
		return "";
	}
	@ConfigItem(
		keyName = "useIgnoreList",
		name = "Ignore by ID",
		description = "Never notify for a list of IDs",
		position = 4
	)
	default boolean useIgnoreList()
	{
		return false;
	}

	@ConfigItem(
		keyName = "potionsToIgnore",
		name = "IDs to ignore",
		description = "The IDs of the potions that you never want to be alerted for",
		position = 5
	)
	default String ignoreList()
	{
		return "";
	}
}

