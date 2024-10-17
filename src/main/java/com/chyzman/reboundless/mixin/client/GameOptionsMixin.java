package com.chyzman.reboundless.mixin.client;

import com.chyzman.reboundless.util.ExtraKeyBindingData;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.endec.format.gson.GsonSerializer;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {

    @Shadow @Final static Gson GSON;

    @Inject(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;getTranslationKey()Ljava/lang/String;", shift = At.Shift.AFTER))
    private void loadExtraKeyBindingData(GameOptions.Visitor visitor, CallbackInfo ci, @Local()KeyBinding keyBinding) {
        var extraData = visitor.visitObject(
                "data.key_" + keyBinding.getTranslationKey(),
                keyBinding.reboundless$getExtraData(),
                (string) -> ExtraKeyBindingData.ENDEC.decodeFully(GsonDeserializer::of, GSON.fromJson(string, JsonElement.class)),
                extraKeyBindingData -> ExtraKeyBindingData.ENDEC.encodeFully(GsonSerializer::of, extraKeyBindingData).toString()
        );
        keyBinding.reboundless$setExtraData(extraData);
    }

}
