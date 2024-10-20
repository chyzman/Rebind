package com.chyzman.reboundless;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Reboundless implements ClientModInitializer {
    public static final String MODID = "reboundless";

    public static final Multimap<InputUtil.Key, KeyBinding> REAL_KEYS_MAP = HashMultimap.create();

    public static final Deque<InputUtil.Key> CURRENTLY_HELD_KEYS = new ConcurrentLinkedDeque<>();

    public static final InputUtil.Key SCROLL_UP = InputUtil.Type.MOUSE.createFromCode(100);
    public static final InputUtil.Key SCROLL_DOWN = InputUtil.Type.MOUSE.createFromCode(101);
    public static final InputUtil.Key SCROLL_LEFT = InputUtil.Type.MOUSE.createFromCode(102);
    public static final InputUtil.Key SCROLL_RIGHT = InputUtil.Type.MOUSE.createFromCode(103);

    public static final List<InputUtil.Key> SCROLL_KEYS = List.of(SCROLL_UP, SCROLL_DOWN, SCROLL_LEFT, SCROLL_RIGHT);

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            SCROLL_KEYS.forEach(key -> {
                KeyBinding.setKeyPressed(key, false);
                CURRENTLY_HELD_KEYS.remove(key);
            });
        });
    }

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }
}
