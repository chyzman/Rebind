package com.chyzman.rebind.pond;

import com.chyzman.rebind.util.ExtraKeyBindingData;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.List;

public interface KeyBindingDuck {

    default KeyBinding rebind$applyExtraData(ExtraKeyBindingData extraData) {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default ExtraKeyBindingData rebind$extractExtraData() {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default List<InputUtil.Key> rebind$getModifiers() {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default KeyBinding rebind$setModifiers(List<InputUtil.Key> modifiers) {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default List<InputUtil.Key> rebind$getDefaultModifiers() {
        throw new UnsupportedOperationException("You shouldn't see this");
    }

    default KeyBinding rebind$setDefaultModifiers(List<InputUtil.Key> defaultModifiers) {
        throw new UnsupportedOperationException("You shouldn't see this");
    }
}
