package com.neuromuser.feedingindicators.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "feedingindicators")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 4, max = 64)
    public int detectionRadius = 16;

    @ConfigEntry.Gui.Tooltip
    public boolean enableHeldItemIndicators = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableLookingIndicators = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 5, max = 100)
    public int cycleDurationTicks = 20;

    @ConfigEntry.Gui.Tooltip
    public double indicatorHeight = 0.5;

    @ConfigEntry.Gui.Tooltip
    public float indicatorScale = 0.5f;

    @ConfigEntry.Gui.Tooltip
    public boolean use3DModels = false;
}