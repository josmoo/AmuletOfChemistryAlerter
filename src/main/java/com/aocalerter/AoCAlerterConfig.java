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
		keyName = "breakNotify",
		name = "Notify sound on break",
		description = "Notify user when amulet of chemistry breaks by sound",
		position = 2
	)
	default boolean breakNotifySound()
	{
		return true;
	}

	@ConfigItem(
		keyName = "breakNotify",
		name = "Notify flash on break",
		description = "Notify user when amulet of chemistry breaks by screen flash",
		position = 3
	)
	default boolean breakNotifyFlash()
	{
		return true;
	}
}
