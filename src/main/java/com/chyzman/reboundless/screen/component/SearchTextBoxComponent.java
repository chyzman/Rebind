package com.chyzman.reboundless.screen.component;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class SearchTextBoxComponent extends TextBoxComponent {
    public SearchTextBoxComponent(Sizing horizontalSizing) {
        super(horizontalSizing);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_TAB) {
            if (MinecraftClient.getInstance().currentScreen != null) {
                MinecraftClient.getInstance().currentScreen.setFocused(MinecraftClient.getInstance().currentScreen);
            }
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            this.setText("");
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
