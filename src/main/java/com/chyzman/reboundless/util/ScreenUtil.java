package com.chyzman.reboundless.util;

import com.chyzman.reboundless.screen.component.ToggleButtonComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class ScreenUtil {

    public static Surface translucentTiledSurface(Identifier texture, int textureWidth, int textureHeight) {
        return (context, component) -> {
            RenderSystem.enableBlend();
            context.drawTexture(texture, component.x(), component.y(), 0, 0, component.width(), component.height(), textureWidth, textureHeight);
            RenderSystem.disableBlend();
        };
    }

    public static final Surface COLLAPSING_SURFACE = (context, component) -> context.fill(
            component.x(),
            component.y(),
            component.x() + 1,
            component.y() + component.height(),
            0x77FFFFFF
    );

    public static ToggleButtonComponent toggleHoldToggler(boolean isCurrentlyToggle, Consumer<Boolean> onChanged) {
        var button = new ToggleButtonComponent(
                Text.translatable("options.key.toggle"),
                Text.translatable("options.key.hold"),
                isCurrentlyToggle
        );
        button.onChanged().subscribe(onChanged::accept);
        return button;
    }

    public static ParentComponent wrapIn20HeightFlow(Component component) {
        return Containers.horizontalFlow(Sizing.content(), Sizing.fixed(20))
                .child(component)
                .verticalAlignment(VerticalAlignment.CENTER);
    }

    public static OverlayContainer<Component> displayConfirmationOverlay(Text prompt, Runnable onConfirm) {
        var overlay = Containers.overlay(null);

        overlay.child(Containers.verticalFlow(Sizing.fill(), Sizing.fill())
                              .child(Components.label(prompt))
                              .child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                                             .child(Components.button(
                                                                    ScreenTexts.YES,
                                                                    buttonComponent -> {
                                                                        overlay.remove();
                                                                        onConfirm.run();
                                                                    }
                                                            )
                                                            .margins(Insets.of(2))
                                             )
                                             .child(Components.button(
                                                                    ScreenTexts.NO,
                                                                    buttonComponent -> overlay.remove()
                                                            )
                                                            .margins(Insets.of(2))
                                             )
                              )
                )
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        overlay.zIndex(100);
        overlay.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                overlay.remove();
                return true;
            }
            return false;
        });

        return overlay;
    }
}
