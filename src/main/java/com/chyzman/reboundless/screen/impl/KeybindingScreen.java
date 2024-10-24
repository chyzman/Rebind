package com.chyzman.reboundless.screen.impl;

import com.chyzman.reboundless.mixin.client.access.KeyBindingAccessor;
import com.chyzman.reboundless.mixin.common.access.ScrollContainerAccessor;
import com.chyzman.reboundless.screen.component.ToggleButtonComponent;
import com.chyzman.reboundless.util.ScreenUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.CommandOpenedScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

import static com.chyzman.reboundless.Reboundless.CURRENTLY_HELD_KEYS;
import static com.chyzman.reboundless.Reboundless.REAL_KEYS_MAP;

@Environment(EnvType.CLIENT)
public class KeybindingScreen extends BaseOwoScreen<FlowLayout> implements CommandOpenedScreen {
    public static final Map<String, Boolean> categoryStates = new HashMap<>();
    public static double scrollAmount = 0;

    @Nullable
    protected final Screen parent;
    protected final GameOptions gameOptions;

    @Nullable
    protected KeyBinding focusedBinding;

    protected final Map<KeyBinding, KeybindConfigurationComponent> keybindComponents = new HashMap<>();
    protected final Map<String, CollapsibleContainer> categoryComponents = new HashMap<>();

    protected ScrollContainer<ParentComponent> scrollContainer = null;

    public KeybindingScreen(@Nullable Screen parent, GameOptions gameOptions) {
        super();
        this.parent = parent;
        this.gameOptions = gameOptions;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    protected void build(FlowLayout rootComponent) {

        var sortedKeys = Arrays.stream(gameOptions.allKeys).sorted(KeyBinding::compareTo).toList();

        var categories = sortedKeys.stream().map(KeyBinding::getCategory).distinct().toList();

        var list = Components.list(
                categories,
                flowLayout -> {},
                category -> {
                    var keys = sortedKeys.stream().filter(k -> k.getCategory().equals(category)).toList();
                    var container = new CategoryCollapsibleContainer(
                            Sizing.fill(),
                            Sizing.content(),
                            Text.translatable(category),
                            categoryStates.getOrDefault(category, true)
                    ).child(Components.list(
                            keys,
                            flowLayout -> {},
                            KeybindConfigurationComponent::new,
                            true
                    )).<CollapsibleContainer>configure(collapsible -> collapsible.onToggled().subscribe(nowExpanded -> {
                        categoryStates.put(category, nowExpanded);
                    }));
                    categoryComponents.put(category, container);
                    return container;
                },
                true
        );

        this.scrollContainer = Containers.verticalScroll(
                Sizing.fill(),
                Sizing.fill(),
                Containers.verticalFlow(Sizing.fill(), Sizing.content())
                        .child(list)
                        .padding(Insets.of(3).withRight(16))
        );
        ((ScrollContainerAccessor) scrollContainer).reboundless$setCurrentScrollPosition(scrollAmount);
        ((ScrollContainerAccessor) scrollContainer).reboundless$setScrollOffset(scrollAmount);
        scrollContainer
                .scrollbar(ScrollContainer.Scrollbar.vanillaFlat())
                .scrollbarThiccness(6)
                .padding(Insets.horizontal(1));

        rootComponent
                .child(
                        Containers.verticalFlow(Sizing.fill(), Sizing.fixed(31))
                                .child(
                                        Components.label(Text.translatable("controls.keybinds.title"))
                                                .shadow(true)
                                )
                                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                )
                .child(
                        Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(2))
                                .surface(ScreenUtil.translucentTiledSurface(this.client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, 16, 2))
                )
                .child(
                        Containers.stack(Sizing.fill(100), Sizing.expand())
                                .child(
                                        Containers.horizontalFlow(Sizing.fill(), Sizing.fill())
                                                .child(scrollContainer)
                                                .padding(Insets.horizontal(50))
                                                .surface(ScreenUtil.translucentTiledSurface(this.client.world == null ? EntryListWidget.MENU_LIST_BACKGROUND_TEXTURE : EntryListWidget.INWORLD_MENU_LIST_BACKGROUND_TEXTURE, 16, 16))
                                )
                                .child(
                                        Containers.verticalFlow(Sizing.fixed(45), Sizing.fill())
                                                .child(
                                                        Components.button(Text.literal("☐"), button -> {
                                                                    var shouldCollapse = categoryStates.values().stream().allMatch(b -> b);
                                                                    categoryComponents.values().forEach(container -> {
                                                                        if (container.expanded() == shouldCollapse) container.toggleExpansion();
                                                                    });
                                                                })
                                                                .sizing(Sizing.fixed(20))
                                                )
                                                .margins(Insets.of(5))
                                )
                )
                .child(
                        Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(2))
                                .surface(ScreenUtil.translucentTiledSurface(this.client.world == null ? FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, 16, 2))
                )
                .child(Containers.verticalFlow(Sizing.fill(), Sizing.fixed(21))
                               .<FlowLayout>configure(footer -> {
                                   footer.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
                                   footer.margins(Insets.of(5));
                                   footer.padding(Insets.horizontal(50));
                               }))
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    public void updateKeybinds() {
        KeyBinding.updateKeysByCode();
        keybindComponents.forEach((keyBinding, component) -> component.update());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focusedBinding != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                gameOptions.setKeyCode(focusedBinding, InputUtil.UNKNOWN_KEY);
                focusedBinding = null;
                updateKeybinds();
            } else {
                keybindComponents.get(focusedBinding).bindButton.setMessage(editingText(assembleTextFromModifiers(CURRENTLY_HELD_KEYS)));
            }
            return true;
        } else {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.close();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyReleased()) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (focusedBinding != null) {
            keybindComponents.get(focusedBinding).bindButton.setMessage(editingText(assembleTextFromModifiers(CURRENTLY_HELD_KEYS)));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (keyReleased()) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean keyReleased() {
        if (focusedBinding != null && !CURRENTLY_HELD_KEYS.isEmpty()) {
            gameOptions.setKeyCode(focusedBinding, CURRENTLY_HELD_KEYS.getLast());
            var temp = new ArrayList<>(CURRENTLY_HELD_KEYS);
            temp.removeLast();
            focusedBinding.reboundless$getExtraData().modifiers().clear();
            focusedBinding.reboundless$getExtraData().modifiers().addAll(temp);
            focusedBinding = null;
            updateKeybinds();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (keyReleased()) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) {
            this.renderPanoramaBackground(context, delta);
        }
        this.applyBlur(delta);
        this.renderDarkening(context);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        scrollAmount = ((ScrollContainerAccessor) scrollContainer).reboundless$getCurrentScrollPosition();
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void close() {
        //TODO vanilla makes sure any options that are currently being edited are saved
        this.client.setScreen(this.parent);
    }

    @Environment(EnvType.CLIENT)
    public class CategoryCollapsibleContainer extends CollapsibleContainer {
        final Animation<Sizing> toggleAnimation;

        protected CategoryCollapsibleContainer(Sizing horizontalSizing, Sizing verticalSizing, Text title, boolean expanded) {
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

    @Environment(EnvType.CLIENT)
    public class KeybindConfigurationComponent extends FlowLayout {
        final KeyBinding keyBinding;

        final KeyBindButtonComponent bindButton;

        final FlowLayout headerFlow;
        final ButtonComponent resetButton;
        final ButtonComponent settingsButton;

        final FlowLayout settingsFlow;
        final ToggleButtonComponent toggle;
        final ToggleButtonComponent invert;

        boolean fullyOverlaps = false;
        boolean partiallyOverlaps = false;

        protected KeybindConfigurationComponent(KeyBinding keyBinding) {
            super(Sizing.fill(), Sizing.content(), Algorithm.VERTICAL);
            this.keyBinding = keyBinding;

            this.headerFlow = Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(20));
            this.settingsFlow = Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(0));
            this.settingsFlow.verticalAlignment(VerticalAlignment.BOTTOM);

            this.bindButton = new KeyBindButtonComponent(
                    keyBinding.getBoundKeyLocalizedText(),
                    button -> {
                        if (focusedBinding == null) {
                            button.setMessage(editingText(button.getMessage()));
                            focusedBinding = keyBinding;
                            CURRENTLY_HELD_KEYS.clear();
                        }
                    }
            );
            this.resetButton = Components.button(
                    Text.translatable("controls.reboundless.keybinds.keybind.reset"),
                    button -> {
                        focusedBinding = null;
                        gameOptions.setKeyCode(keyBinding, keyBinding.getDefaultKey());
                        keyBinding.reboundless$setExtraData(keyBinding.reboundless$getExtraDataDefaults());
                        updateKeybinds();
                    }
            );
            this.resetButton.sizing(Sizing.fixed(20));

            var settingsAnim = settingsFlow
                    .verticalSizing()
                    .animate(500, Easing.CUBIC, Sizing.content())
                    .backwards();
            settingsAnim.finished().subscribe((direction, looping) -> {
                if (direction.equals(Animation.Direction.BACKWARDS)) settingsFlow.remove();
            });

            this.settingsButton = Components.button(
                    Text.literal("⚙"),
                    button -> {
                        if (!settingsFlow.hasParent()) this.child(settingsFlow);
                        settingsAnim.reverse();
                    }
            );
            this.settingsButton.sizing(Sizing.fixed(20));

            this.headerFlow.child(
                    Components.label(Text.translatable(keyBinding.getTranslationKey()))
                            .shadow(true)
                            .positioning(Positioning.relative(0, 50))
            );
            this.headerFlow.child(
                    Containers.horizontalFlow(Sizing.content(), Sizing.fill())
                            .child(bindButton.margins(Insets.right(3)))
                            .child(resetButton.margins(Insets.right(3)))
                            .child(settingsButton)
                            .positioning(Positioning.relative(100, 50))
                            .verticalAlignment(VerticalAlignment.CENTER)
            );

            this.child(headerFlow);

            this.toggle = ScreenUtil.toggleHoldToggler(
                    keyBinding.reboundless$getExtraData().toggled().isTrue(),
                    nowChecked -> keyBinding.reboundless$getExtraData().toggled().setValue(nowChecked)
            );

            this.invert = new ToggleButtonComponent(
                    Text.translatable("options.true"),
                    Text.translatable("options.false"),
                    keyBinding.reboundless$getExtraData().inverted().isTrue()
            );
            this.invert.onChanged().subscribe(nowChecked -> keyBinding.reboundless$getExtraData().inverted().setValue(nowChecked));

            this.settingsFlow.child(
                    Containers.verticalFlow(Sizing.content(), Sizing.content())
                            .child(ScreenUtil.wrapIn20HeightFlow(
                                    Components.label(Text.translatable("controls.reboundless.keybinds.keybind.toggled"))
                                            .shadow(true).margins(Insets.right(3))))
                            .child(ScreenUtil.wrapIn20HeightFlow(
                                    Components.label(Text.translatable("controls.reboundless.keybinds.keybind.inverted"))
                                            .shadow(true).margins(Insets.right(3))))
            ).child(
                    Containers.verticalFlow(Sizing.content(), Sizing.content())
                            .child(ScreenUtil.wrapIn20HeightFlow(toggle))
                            .child(ScreenUtil.wrapIn20HeightFlow(invert))
            );
            this.settingsFlow.padding(Insets.left(10));
            this.settingsFlow.surface(ScreenUtil.COLLAPSING_SURFACE);

            keybindComponents.put(keyBinding, this);
            update();
        }

        public void update() {
            var extraData = keyBinding.reboundless$getExtraData();
            bindButton.update();
            toggle.enabled(extraData.toggled().isTrue());
            invert.enabled(extraData.inverted().isTrue());
            resetButton.active(!keyBinding.isDefault());
        }

        @Override
        protected void drawChildren(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta, List<? extends Component> children) {
            super.drawChildren(context, mouseX, mouseY, partialTicks, delta, children);

            int m = bindButton.x() - 6;
            if (fullyOverlaps) {
                context.fill(m, y + 1, m + 3, y + bindButton.height() - 1, 100, Colors.RED);
                m -= 4;
            }
            if (partiallyOverlaps) {
                context.fill(m, y + 1, m + 3, y + bindButton.height() - 1, 100, Color.ofDye(DyeColor.ORANGE).argb());
            }
        }

        @Environment(EnvType.CLIENT)
        public class KeyBindButtonComponent extends ButtonComponent {
            protected KeyBindButtonComponent(Text message, Consumer<ButtonComponent> onPress) {
                super(message, onPress);
            }

            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                this.renderer.draw((OwoUIDrawContext) context, this, delta);

                int color = this.active ? 0xffffff : 0xa0a0a0;

                this.drawMessage(context, client.textRenderer, color);
            }

            public void update() {
                fullyOverlaps = false;
                partiallyOverlaps = false;
                var tooltip = Text.empty();
                if (!keyBinding.isUnbound()) {
                    var possibleConflicts = REAL_KEYS_MAP.get(((KeyBindingAccessor) keyBinding).reboundless$getBoundKey());
                    for (KeyBinding possibleConfict : possibleConflicts) {
                        var conflictType = keyBinding.reboundless$conflictsWith(possibleConfict);
                        if (conflictType == ScreenUtil.ConflictType.GUARANTEED) {
                            if (!fullyOverlaps) {
                                tooltip = Text.empty();
                            } else {
                                tooltip.append(Text.literal(", "));
                            }
                            fullyOverlaps = true;
                            tooltip.append(Text.translatable(possibleConfict.getTranslationKey()));
                        } else if (conflictType == ScreenUtil.ConflictType.POSSIBLE) {
                            if (!fullyOverlaps) {
                                if (partiallyOverlaps) tooltip.append(Text.literal(", "));
                                tooltip.append(Text.translatable(possibleConfict.getTranslationKey()));
                            }
                            partiallyOverlaps = true;
                        }
                    }
                }
                var label = assembleTextFromModifiers(keyBinding.reboundless$getExtraData().modifiers());
                label.append(keyBinding.getBoundKeyLocalizedText());
                if (fullyOverlaps || partiallyOverlaps) {
                    label = Text.literal("[ ").append(label.formatted(Formatting.WHITE)).append(" ]").withColor(fullyOverlaps ? Formatting.RED.getColorValue() : DyeColor.ORANGE.getEntityColor());
                    setTooltip(Tooltip.of(Text.translatable("controls.keybinds.keybind." + (fullyOverlaps ? "conflicts" : "possibleConflicts"), tooltip)));
                }
                this.setMessage(label);
            }

            @SuppressWarnings("DataFlowIssue")
            @Override
            public void setMessage(Text message) {
                super.setMessage(message);
                this.setWidth(Math.clamp(client.textRenderer.getWidth(message) + 10, 75, 250));
            }
        }
    }

    public static MutableText assembleTextFromModifiers(Collection<InputUtil.Key> keys) {
        var label = Text.empty();
        keys.forEach(key -> label.append(key.getLocalizedText()).append(" + "));
        return label;
    }

    public static Text editingText(Text text) {
        return Text.literal("> ")
                .append(text.copy().formatted(Formatting.WHITE, Formatting.UNDERLINE))
                .append(" <")
                .formatted(Formatting.YELLOW);
    }
}
