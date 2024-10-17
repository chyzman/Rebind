package com.chyzman.reboundless.util;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.util.Identifier;

public class ScreenUtil {
    public static Surface translucentTiledSurface(Identifier texture, int textureWidth, int textureHeight) {
        return (context, component) -> {
            RenderSystem.enableBlend();
            context.drawTexture(texture, component.x(), component.y(), 0, 0, component.width(), component.height(), textureWidth, textureHeight);
            RenderSystem.disableBlend();
        };
    }
}
