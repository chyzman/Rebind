package com.chyzman.reboundless.mixin.client;

import com.chyzman.reboundless.mixin.client.access.KeyBindingAccessor;
import com.chyzman.reboundless.pond.KeyBindingDuck;
import com.chyzman.reboundless.util.ExtraKeyBindingData;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static com.chyzman.reboundless.client.ReboundlessClient.CURRENTLY_HELD_KEYS;
import static com.chyzman.reboundless.client.ReboundlessClient.REAL_KEYS_MAP;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin implements KeyBindingDuck {
    @Shadow @Final private static Map<String, KeyBinding> KEYS_BY_ID;
    @Shadow private InputUtil.Key boundKey;
    @Shadow public abstract boolean isPressed();
    @Shadow private boolean pressed;

    @Unique private ExtraKeyBindingData extraData;
    @Unique private ExtraKeyBindingData extraDataDefaults = new ExtraKeyBindingData();

    @Inject(method = "<init>(Ljava/lang/String;Lnet/minecraft/client/util/InputUtil$Type;ILjava/lang/String;)V", at = @At(value = "TAIL"))
    private void forwardPutToMultiMap(
            String translationKey,
            InputUtil.Type type,
            int code,
            String category,
            CallbackInfo ci
    ) {
        REAL_KEYS_MAP.put(boundKey, (KeyBinding) (Object) this);
    }

    @Inject(method = "setKeyPressed", at = @At(value = "HEAD"), cancellable = true)
    private static void forwardSetKeyPressedToTheMultiMap(
            InputUtil.Key key,
            boolean pressed,
            CallbackInfo ci
    ) {
        REAL_KEYS_MAP.get(key).forEach(keyBinding -> {
            if (!pressed || CURRENTLY_HELD_KEYS.containsAll(keyBinding.reboundless$getExtraData().modifiers())) keyBinding.setPressed(pressed);
        });
        ci.cancel();
    }

    @Inject(method = "onKeyPressed", at = @At(value = "HEAD"), cancellable = true)
    private static void forwardOnKeyPressedToTheMultiMap(
            InputUtil.Key key,
            CallbackInfo ci
    ) {
        REAL_KEYS_MAP.get(key).forEach(keyBinding -> {
            if (keyBinding.isPressed()) ((KeyBindingAccessor) keyBinding).reboundless$setTimesPressed(((KeyBindingAccessor) keyBinding).reboundless$getTimesPressed() + 1);
        });
        ci.cancel();
    }

    @Inject(method = "updateKeysByCode", at = @At(value = "TAIL"))
    private static void forwardUpdateByCodeToMultiMap(CallbackInfo ci) {
        REAL_KEYS_MAP.clear();
        for (KeyBinding keyBinding : KEYS_BY_ID.values()) {
            REAL_KEYS_MAP.put(((KeyBindingAccessor) keyBinding).reboundless$getBoundKey(), keyBinding);
        }
    }

    @Inject(method = "setPressed", at = @At("HEAD"), cancellable = true)
    private void makeExtraFunctionalityWork(boolean pressed, CallbackInfo ci) {
        ci.cancel();
        if (reboundless$getExtraData().toggled().isTrue()) {
            if (pressed) this.pressed = !isPressed();
        } else {
            this.pressed = pressed;
        }
    }

    @Inject(method = "isDefault", at = @At("RETURN"), cancellable = true)
    private void makeDefaultCheckingCorrect(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && extraData.equals(extraDataDefaults));
    }

    @Override
    public ExtraKeyBindingData reboundless$getExtraData() {
        return extraData;
    }

    @Override
    public KeyBinding reboundless$setExtraData(ExtraKeyBindingData extraData) {
        this.extraData = new ExtraKeyBindingData(extraData);
        return (KeyBinding) (Object) this;
    }

    @Override
    public ExtraKeyBindingData reboundless$getExtraDataDefaults() {
        return extraDataDefaults;
    }

    @Override
    public KeyBinding reboundless$setExtraDataDefaults(ExtraKeyBindingData extraData) {
        this.extraDataDefaults = extraData;
        return (KeyBinding) (Object) this;
    }
}
