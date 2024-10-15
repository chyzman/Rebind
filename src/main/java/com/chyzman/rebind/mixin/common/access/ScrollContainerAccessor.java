package com.chyzman.rebind.mixin.common.access;

import io.wispforest.owo.ui.container.ScrollContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScrollContainer.class)
public interface ScrollContainerAccessor {
    @Accessor(value = "currentScrollPosition", remap = false)
    double rebind$getCurrentScrollPosition();

    @Accessor(value = "currentScrollPosition", remap = false)
    void rebind$setCurrentScrollPosition(double position);

    @Accessor(value = "scrollOffset", remap = false)
    double rebind$getScrollOffset();

    @Accessor(value = "scrollOffset", remap = false)
    void rebind$setScrollOffset(double scrollOffset);
}
