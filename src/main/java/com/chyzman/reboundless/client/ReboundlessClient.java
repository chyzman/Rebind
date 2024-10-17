package com.chyzman.reboundless.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ReboundlessClient implements ClientModInitializer {
    public static final Multimap<InputUtil.Key, KeyBinding> REAL_KEYS_MAP = HashMultimap.create();

    public static final Deque<InputUtil.Key> CURRENTLY_HELD_KEYS = new ConcurrentLinkedDeque<>();

    @Override
    public void onInitializeClient() {
    }
}
