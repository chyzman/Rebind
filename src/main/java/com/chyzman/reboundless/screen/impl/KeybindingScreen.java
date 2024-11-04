package com.chyzman.reboundless.screen.impl;

import com.chyzman.reboundless.api.CategoryMode;
import com.chyzman.reboundless.mixin.client.access.InputUtilKeyAccessor;
import com.chyzman.reboundless.mixin.client.access.KeyBindingAccessor;
import com.chyzman.reboundless.mixin.common.access.ScrollContainerAccessor;
import com.chyzman.reboundless.screen.component.*;
import com.chyzman.reboundless.api.ConflictType;
import com.chyzman.reboundless.util.ScreenUtil;
import com.chyzman.reboundless.api.SortingMode;
import com.chyzman.reboundless.util.StringUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
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
import net.minecraft.screen.ScreenTexts;
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

    public static SortingMode sortingMode = SortingMode.VANILLA;
    public static boolean sortInverted = false;
    public static CategoryMode categoryMode = CategoryMode.VANILLA;
    public static String searchTerm = "";

    @Nullable
    protected final Screen parent;
    protected final GameOptions gameOptions;

    @Nullable
    protected KeyBinding focusedBinding;

    protected final Map<KeyBinding, KeybindConfigurationComponent> keybindComponents = new HashMap<>();
    protected final Map<String, CollapsibleContainer> categoryComponents = new HashMap<>();

    protected FlowLayout leftPanel;
    protected ScrollContainer<ParentComponent> scrollContainer = null;
    protected FlowLayout rightPanel;

    protected FlowLayout scrollFlow;

    protected FlowLayout optionsFlow;

    protected SearchTextBoxComponent searchBar;

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

        this.scrollFlow = Containers.verticalFlow(Sizing.fill(), Sizing.content());
        this.scrollFlow
//                .child(this.sortingFlow)
                .padding(Insets.of(3).withRight(16).withTop(0))
                .horizontalAlignment(HorizontalAlignment.CENTER);

        this.scrollContainer = Containers.verticalScroll(
                Sizing.expand(),
                Sizing.fill(),
                this.scrollFlow
        );

        regenerateOptionsList();

        ((ScrollContainerAccessor) scrollContainer).reboundless$setCurrentScrollPosition(scrollAmount);
        ((ScrollContainerAccessor) scrollContainer).reboundless$setScrollOffset(scrollAmount);
        scrollContainer
                .scrollbar(ScrollContainer.Scrollbar.vanillaFlat())
                .scrollbarThiccness(6)
                .padding(Insets.horizontal(1));

        var leftPopout = CollapsiblePopoutComponent.right(Sizing.expand(), false, Sizing.content());
        leftPopout.child(
                        Containers.verticalScroll(
                                Sizing.fixed(100),
                                Sizing.fill(),
                                Components.list(
                                        InputUtilKeyAccessor.reboundless$getKeysMap().values().stream().sorted(Comparator.comparing(key -> key.getLocalizedText().getString())).toList(),
                                        flowLayout -> {},
                                        key -> {
                                            return Components.label(key.getLocalizedText());
                                        },
                                        true
                                )
                        )
                )
                .margins(Insets.top(3));

        var rightPopout = CollapsiblePopoutComponent.left(Sizing.expand(), false, Sizing.fixed(105));
        rightPopout.child(
                        Containers.verticalFlow(Sizing.fixed(100), Sizing.content())
                                .child(
                                        Components.button(Text.translatable("controls.reboundless.keybinds.resetAll"), button -> {
                                                    rootComponent.child(
                                                            ScreenUtil.displayConfirmationOverlay(
                                                                    Text.translatable("controls.reboundless.keybinds.resetAll.confirm"),
                                                                    () -> {
                                                                        for (KeyBinding keyBinding : this.gameOptions.allKeys) {
                                                                            keyBinding.reboundless$setToDefault();
                                                                        }
                                                                        updateKeybinds();
                                                                    }
                                                            )
                                                    );
                                                })
                                                .horizontalSizing(Sizing.fill())
                                )
                                .child(
                                        Components.button(Text.translatable("controls.reboundless.keybinds.clearAll"), button -> {
                                                    rootComponent.child(
                                                            ScreenUtil.displayConfirmationOverlay(
                                                                    Text.translatable("controls.reboundless.keybinds.clearAll.confirm"),
                                                                    () -> {
                                                                        for (KeyBinding keyBinding : this.gameOptions.allKeys) {
                                                                            keyBinding.reboundless$clear();
                                                                        }
                                                                        updateKeybinds();
                                                                    }
                                                            )
                                                    );
                                                })
                                                .horizontalSizing(Sizing.fill())
                                )
                                .child(Components.button(Text.translatable("controls.reboundless.keybinds.reload"), button -> {}))
                )
                .margins(Insets.top(3));

        leftPopout.onToggled().subscribe(nowExpanded -> {
            if (nowExpanded && rightPopout.expanded()) rightPopout.toggleExpansion();
        });
        rightPopout.onToggled().subscribe(nowExpanded -> {
            if (nowExpanded && leftPopout.expanded()) leftPopout.toggleExpansion();
        });


        this.leftPanel = Containers.verticalFlow(Sizing.content(), Sizing.fill());
//        this.leftPanel.child(
//                        Components.button(Text.literal("☐"), button -> {
//                                    var shouldCollapse = categoryStates.values().stream().allMatch(b -> b);
//                                    categoryComponents.values().forEach(container -> {
//                                        if (container.expanded() == shouldCollapse) container.toggleExpansion();
//                                    });
//                                })
//                                .sizing(Sizing.fixed(20))
//                )
//                .margins(Insets.of(5));
        this.leftPanel
                .child(Containers.horizontalFlow(Sizing.fixed(50), Sizing.fixed(0)))
                .child(leftPopout);

        this.rightPanel = Containers.verticalFlow(Sizing.content(), Sizing.fill());
        this.rightPanel.horizontalAlignment(HorizontalAlignment.RIGHT);
        this.rightPanel
                .child(Containers.horizontalFlow(Sizing.fixed(50), Sizing.fixed(0)))
                .child(rightPopout);

        searchBar = new SearchTextBoxComponent(Sizing.fill());
        searchBar.text(searchTerm);
        searchBar.setPlaceholder(Text.translatable("gui.socialInteractions.search_hint").formatted(Formatting.GRAY));
        searchBar.onChanged().subscribe(text -> {
            searchTerm = text;
            regenerateOptionsList();
        });

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
                                                .surface(ScreenUtil.translucentTiledSurface(this.client.world == null ? EntryListWidget.MENU_LIST_BACKGROUND_TEXTURE : EntryListWidget.INWORLD_MENU_LIST_BACKGROUND_TEXTURE, 16, 16))
                                )
                                .child(
                                        Containers.horizontalFlow(Sizing.fill(), Sizing.fill())
                                                .child(leftPanel)
                                                .child(scrollContainer)
                                                .child(rightPanel)
                                )
                )
                .child(
                        Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(2))
                                .surface(ScreenUtil.translucentTiledSurface(this.client.world == null ? FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, 16, 2))
                )
                .child(Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(45))
                               .child(
                                       Containers.verticalFlow(Sizing.fill(60), Sizing.fill())
                                               .child(
                                                       Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(20))
                                                               .child(
                                                                       new EnumButtonComponent<>(
                                                                               SortingMode.class,
                                                                               sortingMode,
                                                                               sortingMode -> Text.translatable("controls.keybinds.sortMode", sortingMode.getLabel()),
                                                                               newMode -> {
                                                                                   sortingMode = newMode;
                                                                                   regenerateOptionsList();
                                                                               }
                                                                       )
                                                                               .horizontalSizing(Sizing.fill(49))
                                                                               .positioning(Positioning.relative(0, 50))
                                                               )
                                                               .child(
                                                                       new EnumButtonComponent<>(
                                                                               CategoryMode.class,
                                                                               categoryMode,
                                                                               sortingMode -> Text.translatable("controls.keybinds.categoryMode", sortingMode.getLabel()),
                                                                               newMode -> {
                                                                                   categoryMode = newMode;
                                                                                   regenerateOptionsList();
                                                                               }
                                                                       )
                                                                               .horizontalSizing(Sizing.fill(49))
                                                                               .positioning(Positioning.relative(100, 50))
                                                               )
                                                               .margins(Insets.horizontal(1))
                                               )
                                               .child(
                                                       searchBar
                                                               .positioning(Positioning.relative(50, 100))
                                               )
                                               .margins(Insets.right(5))
                               )
                               .child(
                                       Components.button(ScreenTexts.DONE, button -> this.close())
                                               .horizontalSizing(Sizing.expand())
                                               .positioning(Positioning.relative(100, 100))
                                               .margins(Insets.bottom(1))
                               )
                               .<FlowLayout>configure(footer -> {
                                   footer.verticalAlignment(VerticalAlignment.CENTER);
                                   footer.margins(Insets.of(5));
                                   footer.padding(Insets.horizontal(50));
                               }))
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    public void updateKeybinds() {
        KeyBinding.updateKeysByCode();
        keybindComponents.forEach((keyBinding, component) -> component.update());
        gameOptions.write();
    }

    public void regenerateOptionsList() {
        keybindComponents.clear();
        categoryComponents.clear();
        if (this.optionsFlow != null) this.optionsFlow.remove();

        var sortedKeys = new ArrayList<>(Arrays.stream(gameOptions.allKeys).sorted(sortInverted ? sortingMode.getComparator().reversed() : sortingMode.getComparator()).toList());

        var categories = new ArrayList<>(sortedKeys.stream().map(keyBinding -> categoryMode.getCategory(keyBinding)).filter(Objects::nonNull).distinct().toList());

        this.optionsFlow = Containers.verticalFlow(Sizing.fill(), Sizing.content());

        var useCategories = searchTerm.isBlank();

        if (useCategories && !categories.isEmpty()) {
            for (String category : categories) {
                var keys = sortedKeys.stream().filter(k -> {
                    var keyCategory = categoryMode.getCategory(k);
                    return keyCategory == null ? category.equals("unknown") : keyCategory.equals(category);
                }).toList();
                sortedKeys.removeAll(keys);

                var container = new SmoothCollapsibleContainer(
                        Sizing.fill(),
                        Sizing.content(),
                        categoryMode.getLabel(category),
                        categoryStates.getOrDefault(category, true)
                );
                container.onToggled().subscribe(nowExpanded -> categoryStates.put(category, nowExpanded));

                for (KeyBinding key : keys) {
                    if (StringUtil.isValidForSearch(searchTerm, Text.translatable(key.getTranslationKey()).getString())) container.child(new KeybindConfigurationComponent(key));
                }

                if (!container.collapsibleChildren().isEmpty()) {
                    this.optionsFlow.child(container);
                    categoryComponents.put(category, container);
                }
            }
            if (!sortedKeys.isEmpty()) {
                var container = new SmoothCollapsibleContainer(
                        Sizing.fill(),
                        Sizing.content(),
                        Text.translatable("key.categories.unknown"),
                        categoryStates.getOrDefault("unknown", true)
                );
                container.onToggled().subscribe(nowExpanded -> categoryStates.put("unknown", nowExpanded));

                for (KeyBinding key : sortedKeys) {
                    if (StringUtil.isValidForSearch(searchTerm, Text.translatable(key.getTranslationKey()).getString())) container.child(new KeybindConfigurationComponent(key));
                }

                if (!container.collapsibleChildren().isEmpty()) {
                    this.optionsFlow.child(container);
                    categoryComponents.put("unknown", container);
                }
            }
        } else {
            for (KeyBinding key : sortedKeys) {
                if (StringUtil.isValidForSearch(searchTerm, Text.translatable(key.getTranslationKey()).getString())) this.optionsFlow.child(new KeybindConfigurationComponent(key));
            }
        }

        this.scrollFlow.child(this.optionsFlow);
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
            if (keyCode == GLFW.GLFW_KEY_ESCAPE && !searchBar.isFocused()) {
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
    public boolean charTyped(char chr, int modifiers) {
        if (focusedBinding != null) return false;
        if (this.uiAdapter.rootComponent.focusHandler() != null && !searchBar.isFocused()) {
            this.uiAdapter.rootComponent.focusHandler().focus(searchBar, Component.FocusSource.MOUSE_CLICK);
            return searchBar.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
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
        if (scrollContainer != null) scrollAmount = ((ScrollContainerAccessor) scrollContainer).reboundless$getCurrentScrollPosition();
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void close() {
        //TODO vanilla makes sure any options that are currently being edited are saved
        this.client.setScreen(this.parent);
    }

    @Environment(EnvType.CLIENT)
    public class KeybindConfigurationComponent extends FlowLayout {
        final KeyBinding keyBinding;

        final KeyBindButtonComponent bindButton;

        final FlowLayout headerFlow;
        final ConfirmingButtonComponent resetButton;
        final ConfirmingButtonComponent clearButton;
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
            this.resetButton = new ConfirmingButtonComponent(
                    Text.translatable("controls.reboundless.keybinds.keybind.reset"),
                    Text.translatable("tooltip.reboundless.keybind.reset.confirm"),
                    button -> {
                        focusedBinding = null;
                        keyBinding.reboundless$setToDefault();
                        updateKeybinds();
                    }
            );
            this.resetButton.sizing(Sizing.fixed(20));

            this.clearButton = new ConfirmingButtonComponent(
                    Text.translatable("controls.reboundless.keybinds.keybind.clear"),
                    Text.translatable("tooltip.reboundless.keybind.clear.confirm"),
                    button -> {
                        focusedBinding = null;
                        keyBinding.reboundless$clear();
                        updateKeybinds();
                    }
            );
            this.clearButton.sizing(Sizing.fixed(20));

            var settingsAnim = settingsFlow
                    .verticalSizing()
                    .animate(500, Easing.CUBIC, Sizing.content())
                    .backwards();
            settingsAnim.finished().subscribe((direction, looping) -> {
                if (direction.equals(Animation.Direction.BACKWARDS)) settingsFlow.remove();
            });

            this.settingsButton = Components.button(
                    Text.translatable("controls.reboundless.keybinds.keybind.settings"),
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
                            .child(clearButton.margins(Insets.right(3)))
                            .child(settingsButton)
                            .positioning(Positioning.relative(100, 50))
                            .verticalAlignment(VerticalAlignment.CENTER)
            );

            this.child(headerFlow);

            this.toggle = ScreenUtil.toggleHoldToggler(
                    keyBinding.reboundless$getExtraData().toggled().isTrue(),
                    nowChecked -> {
                        keyBinding.reboundless$getExtraData().toggled().setValue(nowChecked);
                        updateKeybinds();
                    }
            );

            this.invert = new ToggleButtonComponent(
                    Text.translatable("options.true"),
                    Text.translatable("options.false"),
                    keyBinding.reboundless$getExtraData().inverted().isTrue()
            );
            this.invert.onChanged().subscribe(nowChecked -> {
                keyBinding.reboundless$getExtraData().inverted().setValue(nowChecked);
                updateKeybinds();
            });

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
            if (toggle.enabled() != extraData.toggled().isTrue()) toggle.enabled(extraData.toggled().isTrue());
            if (invert.enabled() != extraData.inverted().isTrue()) invert.enabled(extraData.inverted().isTrue());
            resetButton.active(!keyBinding.isDefault());
            clearButton.active(!keyBinding.reboundless$isClear());
        }

        @Override
        protected void drawChildren(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta, List<? extends Component> children) {
            super.drawChildren(context, mouseX, mouseY, partialTicks, delta, children);

            int m = bindButton.x() - 6;
            if (fullyOverlaps) {
                context.fill(m, y + 1, m + 3, y + bindButton.height() - 1, 100, Color.ofDye(DyeColor.ORANGE).argb());
                m -= 4;
            }
            if (partiallyOverlaps) {
                context.fill(m, y + 1, m + 3, y + bindButton.height() - 1, 100, Color.ofRgb(DyeColor.YELLOW.getFireworkColor()).argb());
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
                setTooltip(null);
                fullyOverlaps = false;
                partiallyOverlaps = false;
                var tooltip = Text.empty();
                if (!keyBinding.isUnbound()) {
                    var possibleConflicts = REAL_KEYS_MAP.get(((KeyBindingAccessor) keyBinding).reboundless$getBoundKey());
                    for (KeyBinding possibleConfict : possibleConflicts) {
                        var conflictType = keyBinding.reboundless$conflictsWith(possibleConfict);
                        if (conflictType == ConflictType.GUARANTEED) {
                            if (!fullyOverlaps) {
                                tooltip = Text.empty();
                            } else {
                                tooltip.append(Text.literal(", "));
                            }
                            fullyOverlaps = true;
                            tooltip.append(Text.translatable(possibleConfict.getTranslationKey()));
                        } else if (conflictType == ConflictType.POSSIBLE) {
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
                    label = Text.literal("[ ").append(label.formatted(Formatting.WHITE)).append(" ]").withColor(fullyOverlaps ? DyeColor.ORANGE.getEntityColor() : DyeColor.YELLOW.getFireworkColor());
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
