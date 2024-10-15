package com.chyzman.rebind.screen;

import com.chyzman.rebind.mixin.common.access.ScrollContainerAccessor;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.CommandOpenedScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
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

import static com.chyzman.rebind.client.RebindClient.CURRENTLY_HELD_KEYS;

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
        ((ScrollContainerAccessor) scrollContainer).rebind$setCurrentScrollPosition(scrollAmount);
        ((ScrollContainerAccessor) scrollContainer).rebind$setScrollOffset(scrollAmount);
        scrollContainer
                .scrollbar(ScrollContainer.Scrollbar.vanillaFlat())
                .scrollbarThiccness(6)
                .padding(Insets.of(1));

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
                        Containers.stack(Sizing.fill(101), Sizing.expand())
                                .child(
                                        Containers.horizontalFlow(Sizing.fill(), Sizing.fill())
                                                .child(
                                                        scrollContainer
                                                )
                                                .padding(Insets.horizontal(50))
                                                .surface(Surface.flat(0x77000000).and(Surface.outline(99121212)))
                                )
                                .padding(Insets.of(1))
                                .surface(Surface.outline(0x33FFFFFF))
                )
                .child(Containers.verticalFlow(Sizing.fill(), Sizing.fixed(21))
                        .<FlowLayout>configure(footer -> {
                            footer.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
                            footer.margins(Insets.of(5));
                            footer.padding(Insets.horizontal(50));
                        }))
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .surface(Surface.OPTIONS_BACKGROUND);
    }

    public void updateKeybinds() {
        keybindComponents.forEach((keyBinding, component) -> component.update());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focusedBinding != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                focusedBinding.setBoundKey(InputUtil.UNKNOWN_KEY);
                focusedBinding = null;
                return true;
            } else {
                keybindComponents.get(focusedBinding).updateBindButton(editingText(assembleTextFromModifiers(CURRENTLY_HELD_KEYS)));
                return true;
            }
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
            keybindComponents.get(focusedBinding).updateBindButton(editingText(assembleTextFromModifiers(CURRENTLY_HELD_KEYS)));
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
            focusedBinding.setBoundKey(CURRENTLY_HELD_KEYS.getLast());
            var temp = new ArrayList<>(CURRENTLY_HELD_KEYS);
            temp.removeLast();
            focusedBinding.rebind$setModifiers(temp);
            focusedBinding = null;
            updateKeybinds();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        scrollAmount = ((ScrollContainerAccessor) scrollContainer).rebind$getCurrentScrollPosition();
        //TODO save stuff probably
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void close() {
        //TODO vanilla makes sure any options that are
        this.client.setScreen(this.parent);
    }

    @Environment(EnvType.CLIENT)
    public class KeybindConfigurationComponent extends FlowLayout {
        protected final KeyBinding keyBinding;
        protected final ButtonComponent bindButton;
        protected final ButtonComponent resetButton;

        protected KeybindConfigurationComponent(KeyBinding keyBinding) {
            super(Sizing.fill(), Sizing.fixed(20), Algorithm.HORIZONTAL);
            this.keyBinding = keyBinding;
            this.bindButton = Components.button(
                    keyBinding.getBoundKeyLocalizedText(),
                    button -> {
                        if (focusedBinding == null) {
                            updateBindButton(editingText(button.getMessage()));
                            focusedBinding = keyBinding;
                            CURRENTLY_HELD_KEYS.clear();
                        }
                    }
            );
            bindButton.sizing(Sizing.fixed(75), Sizing.fixed(20));
            this.resetButton = Components.button(
                    Text.literal("⇄"),
                    button -> {
                        focusedBinding = null;
                        keyBinding.setBoundKey(keyBinding.getDefaultKey());
                        keyBinding.rebind$setModifiers(keyBinding.rebind$getDefaultModifiers());
                        update();
                    }
            );
            this.resetButton.sizing(Sizing.fixed(20));
            this.child(Components.label(Text.translatable(keyBinding.getTranslationKey())).shadow(true)
                    .positioning(Positioning.relative(0, 50)));
            this.child(
                    Containers.horizontalFlow(Sizing.content(), Sizing.fill())
                            .child(bindButton.margins(Insets.right(3)))
                            .child(resetButton)
                            .positioning(Positioning.relative(100, 50))
            );
            keybindComponents.put(keyBinding, this);
            update();
        }

        public void update() {
            var label = assembleTextFromModifiers(keyBinding.rebind$getModifiers());
            label.append(keyBinding.getBoundKeyLocalizedText());
            updateBindButton(label);
            resetButton.active(!keyBinding.isDefault());
        }

        @SuppressWarnings("DataFlowIssue")
        public void updateBindButton(Text text) {
            bindButton.setMessage(text);
            bindButton.setWidth(Math.max(75, KeybindingScreen.super.client.textRenderer.getWidth(text) + 10));
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
