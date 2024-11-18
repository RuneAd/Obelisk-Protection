package com.obeliskprotection;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ObeliskProtectionTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(ObeliskProtectionPlugin.class);
        RuneLite.main(args);
    }
} 