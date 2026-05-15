package com.captainslog;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CaptainsLogPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CaptainsLogPlugin.class);
		RuneLite.main(args);
	}
}