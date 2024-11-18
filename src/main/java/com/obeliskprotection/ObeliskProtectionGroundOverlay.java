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
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.Point;
import net.runelite.api.Perspective;

public class ObeliskProtectionGroundOverlay extends Overlay
{
    private final Client client;
    private final ObeliskProtectionPlugin plugin;
    private final ObeliskProtectionConfig config;

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
        if (!config.showGroundMarker() || !plugin.isProtectionActive() || plugin.getObeliskLocation() == null)
        {
            return null;
        }

        LocalPoint loc = plugin.getObeliskLocation();
        if (loc != null)
        {
            Point canvasPoint = Perspective.getCanvasTextLocation(
                client,
                graphics,
                loc,
                "Protection Active",
                0);
            
            if (canvasPoint != null)
            {
                OverlayUtil.renderTextLocation(graphics, canvasPoint, "Protection Active", config.markerColor());
            }
        }

        return null;
    }
} 