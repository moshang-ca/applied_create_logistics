package com.moshang.appliedcreatelogistics.items.LogisticsPattern;

import net.minecraft.world.item.ItemStack;

import java.util.*;

public class LogisticsAddressManager {
    private static final Map<String, List<ItemStack>> ADDRESS_REGISTRY = new HashMap<>();

    public static void registerPatternToAddress(String address, ItemStack patternStack) {
        if(!(patternStack.getItem() instanceof  LogisticsPatternItem)) {
            return;
        }

        ADDRESS_REGISTRY.computeIfAbsent(address, k -> new ArrayList<>()).add(patternStack);
    }

    public static void unregisterPatternFromAddress(String address, ItemStack patternStack) {
        List<ItemStack> patterns = ADDRESS_REGISTRY.get(address);
        if(patterns != null) {
            patterns.removeIf(stack -> ItemStack.matches(stack, patternStack));
            if(patterns.isEmpty()) {
                ADDRESS_REGISTRY.remove(address);
            }
        }
    }

    public static List<ItemStack> getPatternsByAddress(String address) {
        return ADDRESS_REGISTRY.getOrDefault(address, new ArrayList<>());
    }

    public static Set<String> getALLAddresses() {
        return ADDRESS_REGISTRY.keySet();
    }

    public static boolean isValidAddress(String address) {
        return address != null && !address.trim().isEmpty();
    }
}
