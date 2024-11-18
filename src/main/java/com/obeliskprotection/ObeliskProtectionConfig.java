package com.obeliskprotection;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Alpha;
import java.awt.Color;

@ConfigGroup("obeliskprotection")
public interface ObeliskProtectionConfig extends Config
{
    @ConfigItem(
        keyName = "wealthThreshold",
        name = "Wealth Threshold",
        description = "The wealth threshold (excluding 3 most valuable items) above which the obelisk's left-click option will be changed to 'Walk here'"
    )
    default int wealthThreshold()
    {
        return 1000000; // Default 1M GP
    }

    @ConfigItem(
        keyName = "showGroundMarker",
        name = "Show Ground Marker",
        description = "Shows 'Protection Active' text on the ground when protection is active"
    )
    default boolean showGroundMarker()
    {
        return true;
    }

    @Alpha
    @ConfigItem(
        keyName = "markerColor",
        name = "Marker Color",
        description = "Color of the 'Protection Active' text"
    )
    default Color markerColor()
    {
        return Color.RED;
    }
} 