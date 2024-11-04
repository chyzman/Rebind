package com.chyzman.reboundless.api;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

import java.util.Comparator;

public enum SortingMode {
    VANILLA("vanilla", KeyBinding::compareTo, true),
    ALPHABETICAL("alphabetical", Comparator.comparing(keyBinding -> Text.translatable(keyBinding.getTranslationKey()).getString())),
    RECENT("recent", Comparator.comparing(KeyBinding::reboundless$lastConfigured).reversed());

    private final Text label;
    private final Comparator<KeyBinding> comparator;
    private final boolean useCategories;

    SortingMode(String key, Comparator<KeyBinding> comparator, boolean useCategories) {
        this.label = Text.translatable("controls.keybinds.sortMode." + key);
        this.comparator = comparator;
        this.useCategories = useCategories;
    }

    SortingMode(String key, Comparator<KeyBinding> comparator) {
        this(key, comparator, false);
    }

    public Text getLabel() {
        return label;
    }

    public Comparator<KeyBinding> getComparator() {
        return comparator;
    }

    public boolean usesCategories() {
        return useCategories;
    }
}
