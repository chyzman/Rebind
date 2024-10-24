package com.chyzman.reboundless.pond;

import com.chyzman.reboundless.util.ExtraKeyBindingData;
import com.chyzman.reboundless.util.ScreenUtil;
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

    default ScreenUtil.ConflictType reboundless$conflictsWith(KeyBinding other) {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

}
