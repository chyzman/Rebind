package com.chyzman.reboundless.mixin.client;

import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.chyzman.reboundless.client.ReboundlessClient.CURRENTLY_HELD_KEYS;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void rememberMyKeysPlease$addPressedFromMouse(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == 1) {
            var keyInput = InputUtil.Type.MOUSE.createFromCode(button);
            if (!CURRENTLY_HELD_KEYS.contains(keyInput)) CURRENTLY_HELD_KEYS.add(InputUtil.Type.MOUSE.createFromCode(button));
        }
    }

    @Inject(method = "onMouseButton", at = @At("RETURN"))
    private void rememberMyKeysPlease$removeUnpressedFromMouse(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == 0) CURRENTLY_HELD_KEYS.remove(InputUtil.Type.MOUSE.createFromCode(button));
    }
}
