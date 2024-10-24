package com.chyzman.reboundless.screen.component;

import io.wispforest.owo.mixin.ui.access.ClickableWidgetAccessor;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.text.Text;

public class ToggleButtonComponent extends ButtonComponent {
    private boolean enabled;

    private Text disabledMessage;

    protected final EventStream<OnChanged> toggledEvents = OnChanged.newStream();

    public ToggleButtonComponent(Text message, Text disabledMessage, boolean enabled) {
        super(message, buttonComponent -> {});
        this.enabled = enabled;
        this.disabledMessage = disabledMessage;
        this.onPress(buttonComponent -> toggle());
        this.horizontalSizing(Sizing.fixed(75));
    }

    public void setDisabledMessage(Text disabledMessage) {
        this.disabledMessage = disabledMessage;
    }

    public boolean enabled() {
        return enabled;
    }

    public ToggleButtonComponent enabled(boolean enabled) {
        this.enabled = enabled;
        this.toggledEvents.sink().onChanged(this.enabled);
        return this;
    }

    public ToggleButtonComponent toggle() {
        enabled(!enabled);
        return this;
    }

    public EventSource<OnChanged> onChanged() {
        return this.toggledEvents.source();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderer.draw((OwoUIDrawContext) context, this, delta);

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int color = this.active ? 0xffffff : 0xa0a0a0;

        var text = this.enabled ? this.getMessage() : this.disabledMessage;

        if (this.textShadow) {
            context.drawCenteredTextWithShadow(textRenderer, text, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color);
        } else {
            context.drawText(textRenderer, text, (int) (this.getX() + this.width / 2f - textRenderer.getWidth(this.getMessage()) / 2f), (int) (this.getY() + (this.height - 8) / 2f), color, false);
        }

        var tooltip = ((ClickableWidgetAccessor) this).owo$getTooltip();
        if (this.hovered && tooltip.getTooltip() != null)
            context.drawTooltip(textRenderer, tooltip.getTooltip().getLines(MinecraftClient.getInstance()), HoveredTooltipPositioner.INSTANCE, mouseX, mouseY);
    }

    public interface OnChanged {
        void onChanged(boolean nowEnabled);

        static EventStream<OnChanged> newStream() {
            return new EventStream<>(subscribers -> value -> {
                for (var subscriber : subscribers) {
                    subscriber.onChanged(value);
                }
            });
        }
    }
}
