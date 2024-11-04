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

public class CollapsiblePopoutComponent extends FlowLayout {
    protected final EventStream<OnToggled> toggledEvents = OnToggled.newStream();

    protected final Animation<Sizing> toggleAnimation;

    protected final List<Component> collapsibleChildren = new ArrayList<>();
    protected final List<Component> collapsibleChildrenView = Collections.unmodifiableList(this.collapsibleChildren);
    protected boolean expanded;

    protected final ReallySpinnyBoiComponent spinnyBoi;
    protected final FlowLayout toggleLayout;
    protected final FlowLayout contentLayout;

    public static CollapsiblePopoutComponent up(Sizing horizontalSizing, boolean expanded, Sizing expandedSizing) {
        return new CollapsiblePopoutComponent(
                horizontalSizing,
                Sizing.content(),
                true,
                expanded,
                90,
                -90,
                expandedSizing,
                Insets.bottom(3),
                false
        );
    }

    public static CollapsiblePopoutComponent up (Sizing horizontalSizing, boolean expanded) {
        return up(horizontalSizing, expanded, Sizing.content());
    }

    public static CollapsiblePopoutComponent down(Sizing horizontalSizing, boolean expanded, Sizing expandedSizing) {
        return new CollapsiblePopoutComponent(
                horizontalSizing,
                Sizing.content(),
                true,
                expanded,
                -90,
                90,
                expandedSizing,
                Insets.top(3),
                true
        );
    }

    public static CollapsiblePopoutComponent down(Sizing horizontalSizing, boolean expanded) {
        return down(horizontalSizing, expanded, Sizing.content());
    }

    public static CollapsiblePopoutComponent left(Sizing verticalSizing, boolean expanded, Sizing expandedSizing) {
        return new CollapsiblePopoutComponent(
                Sizing.content(),
                verticalSizing,
                false,
                expanded,
                0,
                180,
                expandedSizing,
                Insets.right(3),
                false
        );
    }

    public static CollapsiblePopoutComponent left(Sizing verticalSizing, boolean expanded) {
        return left(verticalSizing, expanded, Sizing.content());
    }

    public static CollapsiblePopoutComponent right(Sizing verticalSizing, boolean expanded, Sizing expandedSizing) {
        return new CollapsiblePopoutComponent(
                Sizing.content(),
                verticalSizing,
                false,
                expanded,
                180,
                0,
                expandedSizing,
                Insets.left(3),
                true
        );
    }

    public static CollapsiblePopoutComponent right(Sizing verticalSizing, boolean expanded) {
        return right(verticalSizing, expanded, Sizing.content());
    }

    protected CollapsiblePopoutComponent(
            Sizing horizontalSizing,
            Sizing verticalSizing,
            boolean vertical,
            boolean expanded,
            float expandedRotation,
            float collapsedRotation,
            Sizing expandedSizing,
            Insets contentPadding,
            boolean contentFirst
    ) {
        super(horizontalSizing, verticalSizing, vertical ? Algorithm.VERTICAL : Algorithm.HORIZONTAL);

        this.toggleLayout = new ToggleFlowLayout(
                vertical ? Sizing.fixed(80) : Sizing.content(),
                vertical ? Sizing.content() : Sizing.fixed(80),
                algorithm
        );
        this.toggleLayout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.toggleLayout.cursorStyle(CursorStyle.HAND);

        this.expanded = expanded;

        this.spinnyBoi = new ReallySpinnyBoiComponent(expandedRotation, collapsedRotation, expanded);
        this.toggleLayout.child(spinnyBoi);

        this.contentLayout = vertical ?
                Containers.horizontalFlow(Sizing.content(), expanded ? expandedSizing : Sizing.fixed(0)) :
                Containers.verticalFlow(expanded ? expandedSizing : Sizing.fixed(0), Sizing.expand());
        this.contentLayout.padding(contentPadding);

        var animatedSizing = vertical ? this.contentLayout.verticalSizing() : this.contentLayout.horizontalSizing();

        this.toggleAnimation = animatedSizing.animate(500, Easing.CUBIC, expanded ? Sizing.fixed(0) : expandedSizing).backwards();
        this.toggleAnimation.finished().subscribe((direction, looping) -> {
            if (direction.equals(expanded ? Animation.Direction.FORWARDS : Animation.Direction.BACKWARDS)) this.contentLayout.clearChildren();
        });

        if (vertical) {
            this.horizontalAlignment(HorizontalAlignment.CENTER);
        } else {
            this.verticalAlignment(VerticalAlignment.CENTER);
        }

        if (contentFirst) {
            super.child(this.contentLayout);
            super.child(this.toggleLayout);
        } else {
            super.child(this.toggleLayout);
            super.child(this.contentLayout);
        }
    }

    public FlowLayout toggleLayout() {
        return this.toggleLayout;
    }

    public FlowLayout contentLayout() {
        return this.contentLayout;
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
        this.spinnyBoi.expanded(!this.expanded);
        if (!expanded) if (contentLayout.children().isEmpty()) this.contentLayout.children(this.collapsibleChildren);

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
        protected final float expandedRotation;
        protected final float collapsedRotation;

        public boolean expanded;
        public float rotation;

        public ReallySpinnyBoiComponent(float expandedRotation, float collapsedRotation, boolean expanded) {
            super(Text.literal(">"));
            this.expandedRotation = expandedRotation;
            this.collapsedRotation = collapsedRotation;
            this.expanded = expanded;
            this.rotation = expanded ? expandedRotation : collapsedRotation;
            this.margins(Insets.of(0, 0, 3, 1));
            this.cursorStyle(CursorStyle.HAND);
        }

        public ReallySpinnyBoiComponent expanded(boolean expanded) {
            this.expanded = expanded;
            return this;
        }

        public boolean expanded() {
            return this.expanded;
        }

        @Override
        public void update(float delta, int mouseX, int mouseY) {
            super.update(delta, mouseX, mouseY);
            this.rotation += Delta.compute(this.rotation, expanded ? expandedRotation : collapsedRotation, delta * .325f);
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

    protected class ToggleFlowLayout extends FlowLayout {
        protected ToggleFlowLayout(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
            super(horizontalSizing, verticalSizing, algorithm);
        }

        @Override
        public boolean onMouseDown(double mouseX, double mouseY, int button) {
            final var superResult = super.onMouseDown(mouseX, mouseY, button);

            if (!superResult) {
                CollapsiblePopoutComponent.this.toggleExpansion();
                UISounds.playInteractionSound();
            }
            return true;
        }
    }
}
