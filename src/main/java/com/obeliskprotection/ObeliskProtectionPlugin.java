package com.obeliskprotection;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import lombok.Getter;

@Slf4j
@PluginDescriptor(
    name = "Obelisk Protection",
    description = "Protects players from accidentally using their POH obelisk when carrying valuable items",
    tags = {"wilderness", "obelisk", "protection", "poh"}
)
public class ObeliskProtectionPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ObeliskProtectionConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ObeliskProtectionGroundOverlay groundOverlay;

    @Getter
    private boolean protectionActive = false;

    @Getter
    private LocalPoint obeliskLocation = null;

    // POH Wilderness Obelisk object ID
    public static final int POH_OBELISK_ID = 31554;

    private static final int[] WILDERNESS_OBELISK_IDS = {
        14826, 14827, 14828, 14829, 14830, 14831  // Wilderness obelisks to ignore
    };

    @Override
    protected void startUp()
    {
        log.info("Obelisk Protection started!");
        overlayManager.add(groundOverlay);
    }

    @Override
    protected void shutDown()
    {
        log.info("Obelisk Protection stopped!");
        overlayManager.remove(groundOverlay);
        protectionActive = false;
        obeliskLocation = null;
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        // Debug every menu entry
        log.debug("Menu Entry - ID: {}, Option: '{}', Target: '{}'", 
            event.getIdentifier(), event.getOption(), event.getTarget());

        // Check if this is an obelisk interaction
        if (!event.getTarget().contains("Obelisk"))
        {
            // Clear protection state when mouse leaves obelisk
            protectionActive = false;
            obeliskLocation = null;
            return;
        }

        // Check if this is a POH obelisk by checking the object ID
        GameObject obelisk = findObelisk();
        if (obelisk == null || obelisk.getId() != POH_OBELISK_ID)
        {
            protectionActive = false;
            obeliskLocation = null;
            return;
        }

        // Calculate risk value regardless of menu option
        int riskValue = calculateRiskValue();
        log.debug("Risk check - Value: {}, Threshold: {}", riskValue, config.wealthThreshold());
        
        if (riskValue > config.wealthThreshold())
        {
            protectionActive = true;
            obeliskLocation = obelisk.getLocalLocation();
            
            // Only remove menu entries for teleport-related options
            String option = event.getOption();
            if (shouldBlockOption(option))
            {
                // Remove the current entry
                MenuEntry[] entries = client.getMenuEntries();
                if (entries.length > 0)
                {
                    // Remove the last entry which is the one that was just added
                    client.setMenuEntries(Arrays.copyOf(entries, entries.length - 1));
                    log.debug("Removed menu option: '{}'", option);
                }
            }
        }
        else
        {
            protectionActive = false;
            obeliskLocation = null;
        }
    }

    private boolean shouldBlockOption(String option)
    {
        if (option == null)
        {
            return false;
        }

        // Match the exact menu options we want to block
        return option.equals("Teleport to destination") ||
               option.equals("Activate") ||
               option.equals("Set destination");
    }

    private int calculateRiskValue()
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        
        List<Integer> protectedValues = new ArrayList<>();
        int totalRiskValue = 0;
        
        if (inventory != null)
        {
            for (Item item : inventory.getItems())
            {
                if (item.getId() != -1)
                {
                    int gePrice = itemManager.getItemPrice(item.getId());
                    ItemComposition itemComp = itemManager.getItemComposition(item.getId());
                    
                    if (itemComp.isStackable() && item.getQuantity() > 3)
                    {
                        // For stackable items, treat the whole stack minus 3 as risk
                        totalRiskValue += gePrice * (item.getQuantity() - 3);
                        // Add single item value to protected values list
                        protectedValues.add(gePrice);
                    }
                    else
                    {
                        // For non-stackable items, add full value to protected values list
                        protectedValues.add(gePrice * item.getQuantity());
                    }
                    
                    log.debug("Inventory item: {} x{} = {}", 
                        itemComp.getName(),
                        item.getQuantity(), 
                        gePrice * item.getQuantity());
                }
            }
        }
        
        if (equipment != null)
        {
            for (Item item : equipment.getItems())
            {
                if (item.getId() != -1)
                {
                    int gePrice = itemManager.getItemPrice(item.getId());
                    ItemComposition itemComp = itemManager.getItemComposition(item.getId());
                    
                    if (itemComp.isStackable() && item.getQuantity() > 3)
                    {
                        // For stackable items, treat the whole stack minus 3 as risk
                        totalRiskValue += gePrice * (item.getQuantity() - 3);
                        // Add single item value to protected values list
                        protectedValues.add(gePrice);
                    }
                    else
                    {
                        // For non-stackable items, add full value to protected values list
                        protectedValues.add(gePrice * item.getQuantity());
                    }
                    
                    log.debug("Equipment item: {} x{} = {}", 
                        itemComp.getName(),
                        item.getQuantity(), 
                        gePrice * item.getQuantity());
                }
            }
        }
        
        // Sort protected values to find the 3 most valuable items
        Collections.sort(protectedValues, Collections.reverseOrder());
        
        // Add any remaining non-protected items to risk value
        for (int i = 3; i < protectedValues.size(); i++)
        {
            totalRiskValue += protectedValues.get(i);
        }
        
        log.debug("Final risk value: {}, Threshold: {}", totalRiskValue, config.wealthThreshold());
        return totalRiskValue;
    }

    private GameObject findObelisk()
    {
        Scene scene = client.getScene();
        Tile[][][] tiles = scene.getTiles();

        for (int z = 0; z < tiles.length; z++)
        {
            for (int x = 0; x < tiles[z].length; x++)
            {
                for (int y = 0; y < tiles[z][x].length; y++)
                {
                    Tile tile = tiles[z][x][y];
                    if (tile == null)
                    {
                        continue;
                    }

                    GameObject[] gameObjects = tile.getGameObjects();
                    if (gameObjects == null)
                    {
                        continue;
                    }

                    for (GameObject obj : gameObjects)
                    {
                        if (obj != null && obj.getId() == POH_OBELISK_ID)
                        {
                            return obj;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Provides
    ObeliskProtectionConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ObeliskProtectionConfig.class);
    }
} 