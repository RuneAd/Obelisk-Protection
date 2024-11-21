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
import net.runelite.api.GameState;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;
import net.runelite.api.Scene;

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

        if (client.getGameState() != GameState.LOGGED_IN || !isInPOH())
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

    private boolean isInPOH()
    {
        int plane = client.getPlane();
        
        LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();
        Scene scene = client.getScene();
        Tile[][][] tiles = scene.getTiles();
        
        int radius = 50;
        int baseX = playerLocation.getSceneX() - radius;
        int baseY = playerLocation.getSceneY() - radius;
        
        for (int x = 0; x < (2 * radius + 1); x++)
        {
            for (int y = 0; y < (2 * radius + 1); y++)
            {
                int checkX = baseX + x;
                int checkY = baseY + y;
                
                if (checkX < 0 || checkY < 0 || checkX >= 104 || checkY >= 104)
                {
                    continue;
                }
                
                Tile tile = tiles[plane][checkX][checkY];
                if (tile != null)
                {
                    GameObject[] objects = tile.getGameObjects();
                    if (objects != null)
                    {
                        for (GameObject obj : objects)
                        {
                            if (obj != null && obj.getId() == ObeliskProtectionPlugin.POH_OBELISK_ID)
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
} 