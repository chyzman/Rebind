package com.chyzman.reboundless;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Reboundless implements ModInitializer {
    public static final String MODID = "reboundless";

    @Override
    public void onInitialize() {
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }
}
