package com.aocalerter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AoCAlerterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AoCAlerterPlugin.class);
		RuneLite.main(args);
	}
}