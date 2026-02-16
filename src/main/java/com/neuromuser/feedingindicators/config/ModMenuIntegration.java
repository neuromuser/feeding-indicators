package com.neuromuser.feedingindicators.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuIntegration implements ModMenuApi {
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            try {
                return AutoConfig.getConfigScreen(ModConfig.class, parent).get();
            } catch (Exception e) {
                return null;
            }
        };
    }
}