package com.chyzman.reboundless.screen.component;

import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Math.signum;

public class EnumButtonComponent<T extends Enum<T>> extends ButtonComponent {
    protected final Class<T> type;
    protected final Function<T, Text> labelGetter;
    protected final Consumer<T> onChanged;
    protected T selected;

    public EnumButtonComponent(Class<T> type, T selected, Function<T, Text> labelGetter, Consumer<T> onChanged) {
        super(Text.empty(), buttonComponent -> {});
        this.type = type;
        this.labelGetter = labelGetter;
        this.setValue(selected);
        this.onChanged = onChanged;
    }

    protected void setValue(T selected) {
        this.selected = selected;
        this.setMessage(labelGetter.apply(selected));
        if (onChanged != null) onChanged.accept(selected);
    }

    protected void cycle(int amount) {
        T[] values = type.getEnumConstants();
        int index = Arrays.asList(values).indexOf(selected);
        index = (index + amount + values.length) % values.length;
        setValue(values[index]);
    }

    @Override
    public void onPress() {
        cycle(Screen.hasShiftDown() ? -1 : 1);
        super.onPress();
    }


    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        cycle((int) signum(amount));
        return true;
    }

    public T selected() {
        return selected;
    }
}
