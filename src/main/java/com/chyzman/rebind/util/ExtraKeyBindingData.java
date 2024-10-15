package com.chyzman.rebind.util;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

public record ExtraKeyBindingData(
        @Nullable List<InputUtil.Key> modifiers,
        @Nullable Boolean toggled
) {
    public static final Endec<ExtraKeyBindingData> ENDEC = StructEndecBuilder.of(
            Endec.STRING.xmap(InputUtil::fromTranslationKey, InputUtil.Key::getTranslationKey).listOf().fieldOf("modifiers", ExtraKeyBindingData::modifiers),
            Endec.BOOLEAN.fieldOf("toggled", ExtraKeyBindingData::toggled),
            ExtraKeyBindingData::new
    );

    public ExtraKeyBindingData() {
        this(List.of(), false);
    }
}
