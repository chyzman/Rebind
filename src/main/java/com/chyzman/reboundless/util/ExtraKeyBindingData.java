package com.chyzman.reboundless.util;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.client.util.InputUtil;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.List;

public record ExtraKeyBindingData(
        List<InputUtil.Key> modifiers,
        MutableBoolean toggled,
        List<InputUtil.Key> exceptions,
        MutableBoolean isWhitelist
) {
    public static final Endec<ExtraKeyBindingData> ENDEC = StructEndecBuilder.of(
            Endec.STRING.xmap(InputUtil::fromTranslationKey, InputUtil.Key::getTranslationKey).listOf().fieldOf("modifiers", ExtraKeyBindingData::modifiers),
            Endec.BOOLEAN.xmap(MutableBoolean::new, MutableBoolean::getValue).fieldOf("toggled", ExtraKeyBindingData::toggled),
            Endec.STRING.xmap(InputUtil::fromTranslationKey, InputUtil.Key::getTranslationKey).listOf().fieldOf("exceptions", ExtraKeyBindingData::exceptions),
            Endec.BOOLEAN.xmap(MutableBoolean::new, MutableBoolean::getValue).fieldOf("isWhitelist", ExtraKeyBindingData::isWhitelist),
            ExtraKeyBindingData::new
    );

    public ExtraKeyBindingData() {
        this(
                new ArrayList<>(),
                new MutableBoolean(),
                new ArrayList<>(),
                new MutableBoolean()
        );
    }

    public ExtraKeyBindingData(ExtraKeyBindingData other) {
        this(
                new ArrayList<>(other.modifiers()),
                new MutableBoolean(other.toggled().getValue()),
                new ArrayList<>(other.exceptions()),
                new MutableBoolean(other.isWhitelist().getValue())
        );
    }
}
