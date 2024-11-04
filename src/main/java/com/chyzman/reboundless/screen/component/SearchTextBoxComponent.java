package com.chyzman.reboundless.screen.component;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Sizing;
import org.lwjgl.glfw.GLFW;

public class SearchTextBoxComponent extends TextBoxComponent {
    public SearchTextBoxComponent(Sizing horizontalSizing) {
        super(horizontalSizing);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_TAB) {
            if (this.focusHandler() != null) this.focusHandler().focus(null, FocusSource.MOUSE_CLICK);
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
