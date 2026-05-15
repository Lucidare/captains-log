package com.captainslog;

import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.captainslog.CaptainsLogPlugin.formatDurationTicks;

@Singleton
public class CaptainsLogPanelOverlay extends OverlayPanel
{
    private final CaptainsLogPlugin plugin;
    private final CaptainsLogConfig config;

    private static final int SKIFF_WORLDVIEW_ID = 2;
    private static final int SLOOP_WORLDVIEW_ID = 3;

    @Inject
    private CaptainsLogPanelOverlay(CaptainsLogPlugin plugin, CaptainsLogConfig config)
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

        List<String> onboardCrew = plugin.getCrewList();
        Map<String, Integer> ticksOnboard = plugin.getTicksOnShip();
        List<Map.Entry<String, Integer>> allList = new ArrayList<>(ticksOnboard.entrySet());

        if (config.sortAlphabetically())
        {
            allList.sort(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER));
        } else {
            allList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        }

        // Title
        String title = "Crew List";
        panelComponent.getChildren().add(
                TitleComponent.builder()
                        .text(title)
                        .color(Color.WHITE)
                        .build()
        );

        if (!plugin.isOnBoat()) {
            panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left("Paused:")
                            .right("Return to ship")
                            .leftColor(Color.WHITE)
                            .rightColor(Color.CYAN)
                            .build()
            );
        }

        // Capacity
        int total;
        switch (plugin.getBoatType()) {
            case 1: // raft
                total = 2;
                break;
            case 2: // Skiff
                total = 5;
                break;
            case 3: // Sloop
                total = 10;
                break;
            default:
                total = -1;
        }
        if (config.showCount() && total != -1)
        {
            panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left("Crew Capacity:")
                            .right(Integer.toString(onboardCrew.size()) + "/" + total)
                            .leftColor(Color.LIGHT_GRAY)
                            .rightColor(Color.CYAN)
                            .build()
            );
        }

        if (allList.isEmpty())
        {
            panelComponent.getChildren().add(
                    LineComponent.builder()
                            .left("No crew")
                            .leftColor(Color.GRAY)
                            .build()
            );
        }
        else
        {
            for (Map.Entry entry : allList)
            {
                String name = entry.getKey().toString();
                String time = "";
                if (config.showTime()) {
                    Integer ticks = (Integer) entry.getValue();
                    time = formatDurationTicks(ticks);
                }
                Color textColor;
                if (onboardCrew.contains(name)) {
                    textColor = config.onboardColor();
                } else {
                    textColor = config.offboardColor();
                }
                panelComponent.getChildren().add(
                        LineComponent.builder()
                                .left(name)
                                .right(time)
                                .leftColor(textColor)
                                .rightColor(Color.WHITE)
                                .build()
                );
            }
        }

        return super.render(graphics);
    }
}
