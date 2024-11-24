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
import java.util.*;
import lombok.Getter;
import com.google.common.collect.ImmutableSet;

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

    private static final Set<Integer> POH_REGIONS = ImmutableSet.of(7257, 7513, 7514, 7769, 7770, 8025, 8026);

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
        if (!isInPOH()) {
            resetProtection();
            return;
        }

        if (!isObeliskRelated(event.getTarget())) {
            if (client.getMenuEntries().length == 1) {
                resetProtection();
            }
            return;
        }

        GameObject obelisk = findObelisk();
        if (obelisk == null || obelisk.getId() != POH_OBELISK_ID) {
            resetProtection();
            return;
        }

        int riskValue = calculateRiskValue();
        if (riskValue > config.wealthThreshold()) {
            activateProtection(obelisk, event);
        } else {
            resetProtection();
        }
    }

    private void activateProtection(GameObject obelisk, MenuEntryAdded event) {
        protectionActive = true;
        obeliskLocation = obelisk.getLocalLocation();
        
        if (shouldBlockOption(event.getOption().toLowerCase())) {
            removeLastMenuEntry();
        }
    }

    private void resetProtection() {
        protectionActive = false;
        obeliskLocation = null;
    }

    private boolean shouldBlockOption(String option) {
        if (option == null) return false;

        Set<String> blockedOptions = Set.of("teleport to destination", "activate", "set destination");
        return blockedOptions.contains(option.toLowerCase());
    }

    private int calculateRiskValue() {
        List<ItemContainer> containers = List.of(
                client.getItemContainer(InventoryID.INVENTORY),
                client.getItemContainer(InventoryID.EQUIPMENT)
        );

        List<Integer> protectedValues = new ArrayList<>();
        int totalRiskValue = 0;

        for (ItemContainer container : containers) {
            if (container == null) continue;

            for (Item item : container.getItems()) {
                if (item.getId() == -1) continue;

                int gePrice = itemManager.getItemPrice(item.getId());
                ItemComposition itemComp = itemManager.getItemComposition(item.getId());

                int riskAmount = itemComp.isStackable() && item.getQuantity() > 3
                        ? gePrice * (item.getQuantity() - 3)
                        : gePrice * item.getQuantity();

                totalRiskValue += riskAmount;
                protectedValues.add(gePrice);
            }
        }

        // Sort protected values and add all non-protected items to risk value
        protectedValues.sort(Collections.reverseOrder());
        for (int i = 3; i < protectedValues.size(); i++) {
            totalRiskValue += protectedValues.get(i);
        }

        return totalRiskValue;
    }

    private boolean isInPOH() {
        if (!client.isInInstancedRegion()) {
            return false;
        }

        WorldPoint instancePoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
        return instancePoint != null && POH_REGIONS.contains(instancePoint.getRegionID());
    }

    private GameObject findObelisk() {
        if (!isInPOH()) {
            return null;
        }

        Scene scene = client.getScene();
        int plane = client.getPlane();

        for (Tile[] tiles : scene.getTiles()[plane]) {
            for (Tile tile : tiles) {
                if (tile == null) continue;

                for (GameObject obj : tile.getGameObjects()) {
                    if (obj != null && obj.getId() == POH_OBELISK_ID) {
                        return obj;
                    }
                }
            }
        }
        return null;
    }

    private boolean isObeliskRelated(String target) {
        if (target == null) return false;

        String lowerTarget = target.toLowerCase();
        return lowerTarget.contains("obelisk") || lowerTarget.contains("wilderness portal");
    }

    private void removeLastMenuEntry() {
        MenuEntry[] updatedEntries = client.getMenuEntries();
        if (updatedEntries.length > 0) {
            client.setMenuEntries(Arrays.copyOf(updatedEntries, updatedEntries.length - 1));
        }
    }

    @Provides
    ObeliskProtectionConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ObeliskProtectionConfig.class);
    }
}
