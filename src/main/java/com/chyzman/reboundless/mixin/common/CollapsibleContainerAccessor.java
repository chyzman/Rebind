package com.chyzman.reboundless.mixin.common;

import com.chyzman.reboundless.screen.impl.KeybindingScreen;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(CollapsibleContainer.class)
public abstract class CollapsibleContainerAccessor {

    @Shadow(remap = false) @Final protected FlowLayout contentLayout;

    @WrapOperation(method = "toggleExpansion", at = @At(value = "INVOKE", target = "Lio/wispforest/owo/ui/container/FlowLayout;clearChildren()Lio/wispforest/owo/ui/container/FlowLayout;"), remap = false)
    public FlowLayout dontBreakCategoryCollapsibleContainer$dontClear(FlowLayout instance, Operation<FlowLayout> original) {
        if (!(((CollapsibleContainer) (Object) this) instanceof KeybindingScreen.CategoryCollapsibleContainer)) original.call(instance);
        return instance;
    }

    @WrapOperation(method = "toggleExpansion", at = @At(value = "INVOKE", target = "Lio/wispforest/owo/ui/container/FlowLayout;children(Ljava/util/Collection;)Lio/wispforest/owo/ui/container/FlowLayout;"), remap = false)
    public FlowLayout dontBreakCategoryCollapsibleContainer$correctlyAddChildren(FlowLayout instance, Collection<? extends Component> children, Operation<FlowLayout> original) {
        if (!(((CollapsibleContainer) (Object) this) instanceof KeybindingScreen.CategoryCollapsibleContainer) || contentLayout.children().isEmpty()) original.call(instance, children);
        return instance;
    }

}
