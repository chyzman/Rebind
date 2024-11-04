package com.chyzman.reboundless.pond;

import com.chyzman.reboundless.api.ConflictType;
import com.chyzman.reboundless.api.ExtraKeyBindingData;
import net.minecraft.client.option.KeyBinding;

public interface KeyBindingDuck {

    default ExtraKeyBindingData reboundless$getExtraData() {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default KeyBinding reboundless$setExtraData(ExtraKeyBindingData extraData) {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default ExtraKeyBindingData reboundless$getExtraDataDefaults() {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default KeyBinding reboundless$setExtraDataDefaults(ExtraKeyBindingData extraData) {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default long reboundless$lastConfigured() {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default KeyBinding reboundless$updateLastConfigured() {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default boolean reboundless$isVanilla() {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default ConflictType reboundless$conflictsWith(KeyBinding other) {
        throw new UnsupportedOperationException("You shouldn't see this");
    }
}
