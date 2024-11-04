package com.chyzman.reboundless.api;

import com.chyzman.reboundless.Reboundless;
import com.chyzman.reboundless.mixin.client.access.KeyBindingRegistryImplAccessor;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

import java.util.function.Function;

public enum CategoryMode {
    VANILLA("vanilla", KeyBinding::getCategory, Text::translatable),
    MOD("mod", keyBinding -> {
        if (!(KeyBindingRegistryImplAccessor.reboundless$getModdedKeys().contains(keyBinding))) return "minecraft";
        var pattern = Reboundless.TRADITIONAL_KEYBIND_KEY_PATTERN.matcher(keyBinding.getTranslationKey());
        if (pattern.find()) return pattern.group(1);
        return null;
    }, keyBinding -> {
        if ((Reboundless.MOD_NAME_MAP.containsKey(keyBinding))) return Text.of(Reboundless.MOD_NAME_MAP.get(keyBinding));
        return Text.of("categories.unknown");
    }),
    NONE("none", keyBinding -> null,s -> null);

    private final Text label;
    private final Function<KeyBinding, String> category;
    private final Function<String, Text> labelGetter;

    CategoryMode(String key, Function<KeyBinding, String> category, Function<String, Text> labelGetter) {
        this.label = Text.translatable("controls.keybinds.categoryMode." + key);
        this.category = category;
        this.labelGetter = labelGetter;
    }

    public Text getLabel() {
        return label;
    }

    public String getCategory(KeyBinding keyBinding) {
        return category.apply(keyBinding);
    }

    public Text getLabel(String category) {
        return labelGetter.apply(category);
    }
}
