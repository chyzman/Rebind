package com.chyzman.reboundless.util;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.client.util.InputUtil;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record ExtraKeyBindingData(
        List<InputUtil.Key> modifiers,
        MutableBoolean toggled
) {
    public static final Endec<ExtraKeyBindingData> ENDEC = StructEndecBuilder.of(
            Endec.STRING.xmap(InputUtil::fromTranslationKey, InputUtil.Key::getTranslationKey).listOf().fieldOf("modifiers", ExtraKeyBindingData::modifiers),
            Endec.BOOLEAN.xmap(MutableBoolean::new, MutableBoolean::getValue).fieldOf("toggled", ExtraKeyBindingData::toggled),
            ExtraKeyBindingData::new
    );

    public ExtraKeyBindingData() {
        this(new ArrayList<>(), new MutableBoolean());
    }
}
