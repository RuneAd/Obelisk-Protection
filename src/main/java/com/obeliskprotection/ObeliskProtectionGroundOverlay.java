package com.obeliskprotection;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Set;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.Point;
import net.runelite.api.Perspective;
import net.runelite.api.GameState;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;
import net.runelite.api.Scene;
import com.google.common.collect.ImmutableSet;
import net.runelite.api.coords.WorldPoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObeliskProtectionGroundOverlay extends Overlay
{
    private final Client client;
    private final ObeliskProtectionPlugin plugin;
    private final ObeliskProtectionConfig config;

    private static final Set<Integer> POH_REGIONS = ImmutableSet.of(7257, 7513, 7514, 7769, 7770, 8025, 8026);

    @Inject
    private ObeliskProtectionGroundOverlay(Client client, ObeliskProtectionPlugin plugin, ObeliskProtectionConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showGroundMarker())
        {
            log.debug("Ground marker disabled in config");
            return null;
        }
        
        if (!plugin.isProtectionActive())
        {
            log.debug("Protection not active in plugin");
            return null;
        }
        
        LocalPoint loc = plugin.getObeliskLocation();
        if (loc == null)
        {
            log.debug("Obelisk location is null in plugin");
            return null;
        }

        if (!isInPOH())
        {
            log.debug("Not in POH - Region ID: {}", client.getLocalPlayer().getWorldLocation().getRegionID());
            return null;
        }

        if (client.getGameState() != GameState.LOGGED_IN)
        {
            log.debug("Not logged in - Game state: {}", client.getGameState());
            return null;
        }

        Point canvasPoint = Perspective.getCanvasTextLocation(
            client,
            graphics,
            loc,
            "Protection Active",
            0);
        
        if (canvasPoint != null)
        {
            OverlayUtil.renderTextLocation(graphics, canvasPoint, "Protection Active", config.markerColor());
            log.debug("Rendered 'Protection Active' at {} for location {}", canvasPoint, loc);
        }
        else
        {
            log.debug("Could not get canvas point for location {}", loc);
        }

        return null;
    }

    private boolean isInPOH()
    {
        if (!client.isInInstancedRegion())
        {
            log.debug("Not in instanced region");
            return false;
        }

        WorldPoint worldPoint = client.getLocalPlayer().getWorldLocation();
        WorldPoint instancePoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
        
        int regionId = worldPoint.getRegionID();
        int instanceRegionId = instancePoint != null ? instancePoint.getRegionID() : -1;
        
        log.debug("Region check - World Region: {}, Instance Region: {}, POH Regions: {}", 
            regionId, instanceRegionId, POH_REGIONS);
        
        return instancePoint != null && POH_REGIONS.contains(instanceRegionId);
    }
} 