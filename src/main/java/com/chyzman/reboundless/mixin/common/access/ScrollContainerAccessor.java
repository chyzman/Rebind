package com.chyzman.reboundless.mixin.common.access;

import io.wispforest.owo.ui.container.ScrollContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScrollContainer.class)
public interface ScrollContainerAccessor {
    @Accessor(value = "currentScrollPosition", remap = false)
    double reboundless$getCurrentScrollPosition();

    @Accessor(value = "currentScrollPosition", remap = false)
    void reboundless$setCurrentScrollPosition(double position);

    @Accessor(value = "scrollOffset", remap = false)
    double reboundless$getScrollOffset();

    @Accessor(value = "scrollOffset", remap = false)
    void reboundless$setScrollOffset(double scrollOffset);
}
