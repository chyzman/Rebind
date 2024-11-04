package com.chyzman.reboundless.screen.component;

import io.wispforest.owo.mixin.ui.access.ClickableWidgetAccessor;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class ConfirmingButtonComponent extends ButtonComponent {
    private boolean confirming = false;
    private Text confirmMessage;

    public ConfirmingButtonComponent(Text message, Text confirmMessage, Consumer<ButtonComponent> onPress) {
        super(message, onPress);
        this.confirmMessage = confirmMessage;
        this.focusLost().subscribe(() -> {
            confirming = false;
            this.setTooltip(null);
        });
        this.mouseLeave().subscribe(() -> {
            confirming = false;
            this.setTooltip(null);
        });
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (confirming) {
            confirming = false;
            this.setTooltip(null);
            super.onClick(mouseX, mouseY);
        } else {
            confirming = true;
            this.setTooltip(Tooltip.of(this.confirmMessage));
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderer.draw((OwoUIDrawContext) context, this, delta);

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int color = this.active ? 0xffffff : 0xa0a0a0;
        if (confirming) color = Formatting.RED.getColorValue();

        if (this.textShadow) {
            context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color);
        } else {
            context.drawText(textRenderer, this.getMessage(), (int) (this.getX() + this.width / 2f - textRenderer.getWidth(this.getMessage()) / 2f), (int) (this.getY() + (this.height - 8) / 2f), color, false);
        }
    }


}
