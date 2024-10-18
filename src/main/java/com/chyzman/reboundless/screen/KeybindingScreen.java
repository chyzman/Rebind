package com.chyzman.reboundless.screen;

import com.chyzman.reboundless.mixin.common.access.ScrollContainerAccessor;
import com.chyzman.reboundless.util.ScreenUtil;
import io.wispforest.owo.mixin.ui.access.ClickableWidgetAccessor;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.CommandOpenedScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

import static com.chyzman.reboundless.client.ReboundlessClient.CURRENTLY_HELD_KEYS;

@Environment(EnvType.CLIENT)
public class KeybindingScreen extends BaseOwoScreen<FlowLayout> implements CommandOpenedScreen {
    public static final Map<String, Boolean> categoryStates = new HashMap<>();
    public static double scrollAmount = 0;

    @Nullable
    protected final Screen parent;
    protected final GameOptions gameOptions;

    @Nullable
    protected KeyBinding focusedBinding;

    protected boolean anyCategoriesExpanded = categoryStates.values().stream().anyMatch(b -> b);

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
                    var container = Containers.collapsible(
                            Sizing.fill(),
                            Sizing.content(),
                            Text.translatable(category),
                            categoryStates.getOrDefault(category, true)
                    ).child(Components.list(
                            keys,
                            flowLayout -> {},
                            KeybindConfigurationComponent::new,
                            true
                    )).<CollapsibleContainer>configure(collapsible -> collapsible.onToggled().subscribe(nowExpanded -> categoryStates.put(category, nowExpanded)));
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
                                        Containers.verticalFlow(Sizing.fixed(45),Sizing.fill())
                                                .child(
                                                        Components.button(Text.literal("\uD83D\uDDD1"), button -> {
                                                            var toggled = categoryComponents.values().stream().toList().getFirst().expanded();
                                                            categoryComponents.values().forEach(container -> {
                                                                if (container.expanded() == toggled) container.toggleExpansion();
                                                            });
                                                        })
                                                                .sizing(Sizing.fixed(20))
                                                )
                                                .margins(Insets.of(5))
                                )
                )
                .child(
                        Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(2))
                                  .surface(ScreenUtil.translucentTiledSurface(this.client.world == null ?FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, 16, 2))
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
    public class KeybindConfigurationComponent extends FlowLayout {
        protected final KeyBinding keyBinding;
        protected final KeyBindButtonComponent bindButton;
        protected final ButtonComponent resetButton;
        protected final CheckboxComponent toggledCheck;
        protected boolean duplicateKey = false;
        protected int overlappingModifiers = 0;

        protected KeybindConfigurationComponent(KeyBinding keyBinding) {
            super(Sizing.fill(), Sizing.fixed(20), Algorithm.HORIZONTAL);
            this.keyBinding = keyBinding;
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
                    Text.literal("⇄"),
                    button -> {
                        focusedBinding = null;
                        gameOptions.setKeyCode(keyBinding, keyBinding.getDefaultKey());
                        keyBinding.reboundless$setExtraData(keyBinding.reboundless$getExtraDataDefaults());
                        updateKeybinds();
                    }
            );
            this.resetButton.sizing(Sizing.fixed(20));
            this.toggledCheck = Components.checkbox(Text.empty()).checked(keyBinding.reboundless$getExtraData().toggled().isTrue());
            this.toggledCheck.onChanged(nowChecked -> keyBinding.reboundless$getExtraData().toggled().setValue(nowChecked));
            this.child(Components.label(Text.translatable(keyBinding.getTranslationKey())).shadow(true)
                                 .positioning(Positioning.relative(0, 50)));
            this.child(
                    Containers.horizontalFlow(Sizing.content(), Sizing.fill())
                              .child(bindButton.margins(Insets.right(3)))
                              .child(resetButton)
                              .child(toggledCheck)
                              .positioning(Positioning.relative(100, 50))
                              .verticalAlignment(VerticalAlignment.CENTER)
            );
            keybindComponents.put(keyBinding, this);
            update();
        }

        public void update() {
            var extraData = keyBinding.reboundless$getExtraData();
            bindButton.update();
            toggledCheck.checked(extraData.toggled().isTrue());
            resetButton.active(!keyBinding.isDefault());
        }

        @Environment(EnvType.CLIENT)
        public class KeyBindButtonComponent extends ButtonComponent {
            protected KeyBindButtonComponent(Text message, Consumer<ButtonComponent> onPress) {
                super(message, onPress);
            }

            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                this.renderer.draw((OwoUIDrawContext) context, this, delta);

                var textRenderer = MinecraftClient.getInstance().textRenderer;
                int color = this.active ? 0xffffff : 0xa0a0a0;

                this.drawMessage(context, client.textRenderer, color);

                var tooltip = ((ClickableWidgetAccessor) this).owo$getTooltip();
                if (this.hovered && tooltip.getTooltip() != null)
                    context.drawTooltip(textRenderer, tooltip.getTooltip().getLines(MinecraftClient.getInstance()), HoveredTooltipPositioner.INSTANCE, mouseX, mouseY);

                if (duplicateKey) {
                    int l = 3;
                    int m = x - 6;
                    context.fill(m, y - 1, m + 3, y + height, -65536);
                }
            }

            public void update() {
                var label = assembleTextFromModifiers( keyBinding.reboundless$getExtraData().modifiers());
                label.append(keyBinding.getBoundKeyLocalizedText());
                if (duplicateKey) {
                    label = Text.literal("[ ").append(label.formatted(Formatting.WHITE)).append(" ]").formatted(Formatting.RED);

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
