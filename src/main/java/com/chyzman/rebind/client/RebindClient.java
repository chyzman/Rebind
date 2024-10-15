package com.chyzman.rebind.client;

import com.chyzman.rebind.screen.KeybindingScreen;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class RebindClient implements ClientModInitializer {
    public static final Multimap<InputUtil.Key, KeyBinding> REAL_KEYS_MAP = HashMultimap.create();

    public static final Deque<InputUtil.Key> CURRENTLY_HELD_KEYS = new ConcurrentLinkedDeque<>();

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("vanillaControls").executes(context -> {
                var source = context.getSource();
                source.getClient().setScreen(new KeybindsScreen(null, source.getClient().options));
                return 1;
            }));
        });
    }
}
