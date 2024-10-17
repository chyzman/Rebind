package com.chyzman.reboundless.mixin.client;

import com.chyzman.reboundless.util.ScreenModificationUtil;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(AccessibilityOptionsScreen.class)
public abstract class AccessibilityOptionsScreenMixin {
    @Inject(method = "getOptions", at = @At("RETURN"), cancellable = true, order = 0)
    private static void injectGetDownOptionsIntoAccessibilityScreen(GameOptions gameOptions, CallbackInfoReturnable<SimpleOption<?>[]> cir) {
//        try {
            var listed = Arrays.asList(cir.getReturnValue());
            cir.setReturnValue(ScreenModificationUtil.removeToggleButtonsFrom(
                    listed.indexOf(gameOptions.getBobView()) + 1,
                    listed.indexOf(gameOptions.getDistortionEffectScale()),
                    cir.getReturnValue()
            ));
//        } catch (Exception e) {
//            throw new UnsupportedOperationException("Failed to remove options from AccessibilityOptionsScreen", e);
//        }
    }
}
