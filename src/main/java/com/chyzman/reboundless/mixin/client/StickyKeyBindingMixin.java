package com.chyzman.reboundless.mixin.client;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(StickyKeyBinding.class)
public abstract class StickyKeyBindingMixin extends KeyBinding {
    @Shadow @Final private BooleanSupplier toggleGetter;
    @Unique boolean startingValue;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setStartingValue(String id, int code, String category, BooleanSupplier toggleGetter, CallbackInfo ci) {
        var toggled = toggleGetter.getAsBoolean();
        this.startingValue = toggled;
        this.reboundless$getExtraDataDefaults().toggled().setValue(toggled);
    }

    public StickyKeyBindingMixin(String translationKey, int code, String category, boolean startingValue) {
        super(translationKey, code, category);
    }

    @Inject(method = "setPressed", at = @At("HEAD"), cancellable = true)
    private void makeExtraFunctionalityWork(boolean pressed, CallbackInfo ci) {
        var toggled = toggleGetter.getAsBoolean();
        if (startingValue != toggled) {
            startingValue = toggled;
            //TODO open overlay screen with alert that the toggled configuration has been overridden
        }
        ci.cancel();
        super.setPressed(pressed);
    }
}
