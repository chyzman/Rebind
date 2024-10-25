package com.chyzman.reboundless.screen.component;

import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.core.Animation;
import io.wispforest.owo.ui.core.Easing;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SmoothCollapsibleContainer extends CollapsibleContainer {
    final Animation<Sizing> toggleAnimation;

    public SmoothCollapsibleContainer(Sizing horizontalSizing, Sizing verticalSizing, Text title, boolean expanded) {
        super(horizontalSizing, verticalSizing, title, expanded);
        this.contentLayout.verticalSizing(expanded ? Sizing.content() : Sizing.fixed(0));
        this.toggleAnimation = this.contentLayout.verticalSizing().animate(500, Easing.CUBIC, expanded ? Sizing.fixed(0) : Sizing.content()).backwards();
        this.toggleAnimation.finished().subscribe((direction, looping) -> {
            if (direction.equals(expanded ? Animation.Direction.FORWARDS : Animation.Direction.BACKWARDS)) this.contentLayout.clearChildren();
        });
        this.contentLayout.verticalAlignment(VerticalAlignment.BOTTOM);
    }

    @Override
    public void toggleExpansion() {
        super.toggleExpansion();
        this.toggleAnimation.reverse();
    }
}
