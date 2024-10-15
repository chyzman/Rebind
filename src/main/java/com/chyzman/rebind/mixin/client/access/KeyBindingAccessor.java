package com.chyzman.rebind.mixin.client.access;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {

    @Accessor("timesPressed")
    int rebind$getTimesPressed();
    @Accessor("timesPressed")
    void rebind$setTimesPressed(int value);

    @Accessor("boundKey")
    InputUtil.Key rebind$getBoundKey();

}
