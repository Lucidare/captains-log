package com.captainslog;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("CaptainsLog")
public interface CaptainsLogConfig extends Config
{

    @ConfigSection(
            name = "Overlay",
            description = "Configuration settings related to Overlay",
            position = 0
    )
    String overlaySection = "Overlay";

    @ConfigItem(
            keyName = "showOverlay",
            name = "Show Overlay",
            description = "Show the on-screen crew list",
            position = 0,
            section = overlaySection
    )
    default boolean showOverlay()
    {
        return true;
    }


    @ConfigItem(
            keyName = "removeOverlayTicks",
            name = "Remove Overlay Ticks",
            description = "Number of ticks before the player is off the boat before overlay disappears (default 5 min)",
            position = 4,
            section = overlaySection
    )
    default int removeOverlayTicks()
    {
        return 500;
    }
    @ConfigItem(
            keyName = "removeCrewmateTicks",
            name = "Remove Crewmate Ticks",
            description = "Number of ticks before crewmate is removed from crewlist (default 5 min)",
            position = 5,
            section = overlaySection
    )
    default int removeCrewmateTicks()
    {
        return 500;
    }

    @ConfigItem(
            keyName = "showCount",
            name = "Show player count",
            description = "Show the number of crewmates in the overlay header",
            position = 1,
            section = overlaySection
    )
    default boolean showCount()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showTime",
            name = "Show player time",
            description = "Show the time a crewmate is on the ship",
            position = 2,
            section = overlaySection
    )
    default boolean showTime()
    {
        return true;
    }

    @ConfigItem(
            keyName = "sortAlphabetically",
            name = "Sort alphabetically",
            description = "Sort player names alphabetically in the overlay",
            position = 3,
            section = overlaySection
    )
    default boolean sortAlphabetically()
    {
        return false;
    }

    @ConfigSection(
            name = "UI",
            description = "Configuration settings related to UI",
            position = 1
    )
    String UISection = "UI";

    @Alpha
    @ConfigItem(
            keyName = "onboardColor",
            name = "Onboard color",
            description = "Color for onboard crewmmates",
            position = 0,
            section = UISection
    )
    default Color onboardColor()
    {
        return Color.GREEN;
    }

    @Alpha
    @ConfigItem(
            keyName = "offboardColor",
            name = "Offboard color",
            description = "Color for crewmmates no longer onboard",
            position = 1,
            section = UISection
    )
    default Color offboardColor()
    {
        return Color.YELLOW;
    }

    @ConfigSection(
            name = "Debug",
            description = "Debug Section",
            position = 2
    )
    String debugSection = "Debug";

    @ConfigItem(
            keyName = "debug",
            name = "Show debug info",
            description = "Show debug info",
            position = 0,
            section = debugSection
    )
    default boolean showDebugInfo()
    {
        return false;
    }

}
