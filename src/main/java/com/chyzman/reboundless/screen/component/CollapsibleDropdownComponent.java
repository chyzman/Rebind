package com.chyzman.reboundless.screen.component;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Delta;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollapsibleDropdownComponent extends FlowLayout {
    protected final EventStream<OnToggled> toggledEvents = OnToggled.newStream();

    protected final Animation<Sizing> toggleAnimation;

    protected final List<Component> collapsibleChildren = new ArrayList<>();
    protected final List<Component> collapsibleChildrenView = Collections.unmodifiableList(this.collapsibleChildren);
    protected boolean expanded;

    protected final ReallySpinnyBoiComponent spinnyBoi;
    protected final FlowLayout toggleLayout;
    protected final FlowLayout contentLayout;


    public CollapsibleDropdownComponent(Sizing horizontalSizing, boolean expanded) {
        super(horizontalSizing, Sizing.content(), Algorithm.VERTICAL);

        this.toggleLayout = Containers.horizontalFlow(Sizing.fixed(80), Sizing.content());
        this.toggleLayout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.toggleLayout.cursorStyle(CursorStyle.HAND);

        this.spinnyBoi = new ReallySpinnyBoiComponent();
        this.toggleLayout.child(spinnyBoi);

        this.expanded = expanded;
        this.spinnyBoi.targetRotation = expanded ? -90 : 90;
        this.spinnyBoi.rotation = this.spinnyBoi.targetRotation;

        this.contentLayout = Containers.verticalFlow(Sizing.content(), expanded ? Sizing.content() : Sizing.fixed(0));
        this.contentLayout.padding(Insets.top(3));

        this.toggleAnimation = this.contentLayout.verticalSizing().animate(500, Easing.CUBIC, expanded ? Sizing.fixed(0) : Sizing.content()).backwards();
        this.toggleAnimation.finished().subscribe((direction, looping) -> {
            if (direction.equals(expanded ? Animation.Direction.FORWARDS : Animation.Direction.BACKWARDS)) this.contentLayout.clearChildren();
        });

        this.horizontalAlignment(HorizontalAlignment.CENTER);

        super.child(this.contentLayout);

        super.child(this.toggleLayout);
    }

    public FlowLayout toggleLayout() {
        return this.toggleLayout;
    }

    public List<Component> collapsibleChildren() {
        return this.collapsibleChildrenView;
    }

    public boolean expanded() {
        return this.expanded;
    }

    public EventSource<OnToggled> onToggled() {
        return this.toggledEvents.source();
    }

    public void toggleExpansion() {
        if (expanded) {
            this.spinnyBoi.targetRotation = 90;
        } else {
            if (contentLayout.children().isEmpty()) this.contentLayout.children(this.collapsibleChildren);
            this.spinnyBoi.targetRotation = -90;
        }

        this.expanded = !this.expanded;
        this.toggledEvents.sink().onToggle(this.expanded);

        this.toggleAnimation.reverse();
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.KEYBOARD_CYCLE;
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.toggleExpansion();

            super.onKeyPress(keyCode, scanCode, modifiers);
            return true;
        }

        return super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        final var superResult = super.onMouseDown(mouseX, mouseY, button);

        if (mouseY >= (this.fullSize().height() - this.toggleLayout.fullSize().height()) && !superResult) {
            this.toggleExpansion();
            UISounds.playInteractionSound();
            return true;
        } else {
            return superResult;
        }
    }

    @Override
    public FlowLayout child(Component child) {
        this.collapsibleChildren.add(child);
        if (this.expanded) this.contentLayout.child(child);
        return this;
    }

    @Override
    public FlowLayout children(Collection<? extends Component> children) {
        this.collapsibleChildren.addAll(children);
        if (this.expanded) this.contentLayout.children(children);
        return this;
    }

    @Override
    public FlowLayout child(int index, Component child) {
        this.collapsibleChildren.add(index, child);
        if (this.expanded) this.contentLayout.child(index, child);
        return this;
    }

    @Override
    public FlowLayout children(int index, Collection<? extends Component> children) {
        this.collapsibleChildren.addAll(index, children);
        if (this.expanded) this.contentLayout.children(index, children);
        return this;
    }

    @Override
    public FlowLayout removeChild(Component child) {
        this.collapsibleChildren.remove(child);
        return this.contentLayout.removeChild(child);
    }

    public interface OnToggled {
        void onToggle(boolean nowExpanded);

        static EventStream<OnToggled> newStream() {
            return new EventStream<>(subscribers -> nowExpanded -> {
                for (var subscriber : subscribers) {
                    subscriber.onToggle(nowExpanded);
                }
            });
        }
    }

    public static class ReallySpinnyBoiComponent extends LabelComponent {

        public float rotation = 90;
        protected float targetRotation = 90;

        public ReallySpinnyBoiComponent() {
            super(Text.literal(">"));
            this.margins(Insets.of(0, 0, 3, 1));
            this.cursorStyle(CursorStyle.HAND);
        }

        @Override
        public void update(float delta, int mouseX, int mouseY) {
            super.update(delta, mouseX, mouseY);
            this.rotation += Delta.compute(this.rotation, this.targetRotation, delta * .325f);
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            var matrices = context.getMatrices();

            matrices.push();
            matrices.translate(this.x + this.width / 2f - 1, this.y + this.height / 2f - 1, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotation));
            matrices.translate(-(this.x + this.width / 2f - 1), -(this.y + this.height / 2f - 1), 0);

            super.draw(context, mouseX, mouseY, partialTicks, delta);
            matrices.pop();
        }
    }
}
