package com.chyzman.rebind.mixin.client;

import com.chyzman.rebind.mixin.client.access.KeyBindingAccessor;
import com.chyzman.rebind.pond.KeyBindingDuck;
import com.chyzman.rebind.util.ExtraKeyBindingData;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.chyzman.rebind.client.RebindClient.CURRENTLY_HELD_KEYS;
import static com.chyzman.rebind.client.RebindClient.REAL_KEYS_MAP;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin implements KeyBindingDuck {
    @Shadow
    @Final
    private static Map<String, KeyBinding> KEYS_BY_ID;
    @Shadow
    private InputUtil.Key boundKey;

    @Unique
    private List<InputUtil.Key> defaultModifiers = new ArrayList<>();
    @Unique
    private List<InputUtil.Key> modifiers = new ArrayList<>();

    @Inject(method = "isDefault", at = @At("RETURN"), cancellable = true)
    private void makeDefaultCheckingCorrect(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && modifiers.equals(defaultModifiers));
    }

    @Inject(method = "onKeyPressed", at = @At(value = "HEAD"), cancellable = true)
    private static void forwardOnKeyPressedToTheMultiMap(
            InputUtil.Key key,
            CallbackInfo ci
    ) {
        REAL_KEYS_MAP.get(key).forEach(keyBinding -> {
            if (keyBinding.isPressed()) ((KeyBindingAccessor) keyBinding).rebind$setTimesPressed(((KeyBindingAccessor) keyBinding).rebind$getTimesPressed() + 1);
        });
        ci.cancel();
    }

    @Inject(method = "setKeyPressed", at = @At(value = "HEAD"), cancellable = true)
    private static void forwardSetKeyPressedToTheMultiMap(
            InputUtil.Key key,
            boolean pressed,
            CallbackInfo ci
    ) {
        REAL_KEYS_MAP.get(key).forEach(keyBinding -> keyBinding.setPressed(pressed));
        ci.cancel();
    }

    @Inject(method = "updateKeysByCode", at = @At(value = "TAIL"))
    private static void forwardUpdateByCodeToMultiMap(CallbackInfo ci) {
        REAL_KEYS_MAP.clear();
        for (KeyBinding keyBinding : KEYS_BY_ID.values()) {
            REAL_KEYS_MAP.put(((KeyBindingAccessor) keyBinding).rebind$getBoundKey(), keyBinding);
        }
    }

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

    @Inject(method = "setPressed", at = @At("HEAD"), cancellable = true)
    private void makeModifierKeysWork(boolean pressed, CallbackInfo ci) {
        if (pressed && !new HashSet<>(CURRENTLY_HELD_KEYS).containsAll(rebind$getModifiers())) {
            ci.cancel();
        }
    }

    @Override
    public KeyBinding rebind$applyExtraData(ExtraKeyBindingData extraData) {
        this.modifiers = extraData.modifiers() == null ? new ArrayList<>(defaultModifiers) : extraData.modifiers();
        return (KeyBinding) (Object) this;
    }

    @Override
    public ExtraKeyBindingData rebind$extractExtraData() {
        return new ExtraKeyBindingData(modifiers, false);
    }

    @Override
    public List<InputUtil.Key> rebind$getModifiers() {
        return modifiers;
    }

    @Override
    public KeyBinding rebind$setModifiers(List<InputUtil.Key> modifiers) {
        this.modifiers = modifiers;
        return (KeyBinding) (Object) this;
    }

    @Override
    public List<InputUtil.Key> rebind$getDefaultModifiers() {
        return defaultModifiers;
    }

    @Override
    public KeyBinding rebind$setDefaultModifiers(List<InputUtil.Key> defaultModifiers) {
        this.defaultModifiers = defaultModifiers;
        return (KeyBinding) (Object) this;
    }
}
