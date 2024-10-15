package com.chyzman.rebind;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Rebind implements ModInitializer {
    public static final String MODID = "rebind";

    @Override
    public void onInitialize() {
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }
}
