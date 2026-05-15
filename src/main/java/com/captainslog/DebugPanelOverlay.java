package com.captainslog;

import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.List;
import java.util.Map;

@Singleton
public class DebugPanelOverlay extends OverlayPanel
{
    private final CaptainsLogPlugin plugin;
    private final CaptainsLogConfig config;

    @Inject
    private DebugPanelOverlay(CaptainsLogPlugin plugin, CaptainsLogConfig config)
    {
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);

        // ✅ allow Alt-drag & snapping
        setMovable(true);
        setSnappable(true);
        setDragTargetable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        List<String> names = plugin.getCrewList();
        Map<String, Integer> ticksOnboard = plugin.getTicksOnShip();

        // Title
        String title = "Debug";
        panelComponent.getChildren().add(
                TitleComponent.builder()
                        .text(title)
                        .color(Color.WHITE)
                        .build()
        );

        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left("Is on boat:")
                        .right(String.valueOf(plugin.isOnBoat()))
                        .leftColor(Color.LIGHT_GRAY)
                        .rightColor(Color.CYAN)
                        .build()
        );

        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left("Ticks off boat:")
                        .right(String.valueOf(plugin.getTicksOffBoat()))
                        .leftColor(Color.LIGHT_GRAY)
                        .rightColor(Color.CYAN)
                        .build()
        );

        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left("Overlay Ticks:")
                        .right(String.valueOf(config.removeOverlayTicks()))
                        .leftColor(Color.LIGHT_GRAY)
                        .rightColor(Color.CYAN)
                        .build()
        );

        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left("show overlay? ")
                        .right(String.valueOf(plugin.shouldShowOverlay()))
                        .leftColor(Color.LIGHT_GRAY)
                        .rightColor(Color.CYAN)
                        .build()
        );

        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left("keep overlay? ")
                        .right(String.valueOf(plugin.shouldKeepInfo()))
                        .leftColor(Color.LIGHT_GRAY)
                        .rightColor(Color.CYAN)
                        .build()
        );

        panelComponent.getChildren().add(
                LineComponent.builder()
                        .left("ship type: ")
                        .right(plugin.getBoatType().toString())
                        .leftColor(Color.LIGHT_GRAY)
                        .rightColor(Color.CYAN)
                        .build()
        );

        return super.render(graphics);
    }
}
