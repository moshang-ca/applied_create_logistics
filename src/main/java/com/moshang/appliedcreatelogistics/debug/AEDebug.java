package com.moshang.appliedcreatelogistics.debug;

import net.minecraftforge.registries.RegistryObject;

public class AEDebug {
    public static void printPatternClasses() {
        try {
            // 列出所有AE2的物品类
            System.out.println("=== AE2 Item Classes ===");
            for (var field : appeng.init.InitItems.class.getDeclaredFields()) {
                if (field.getType().equals(RegistryObject.class)) {
                    RegistryObject<?> regObj = (RegistryObject<?>) field.get(null);
                    Object item = regObj.get();
                    System.out.println(field.getName() + " -> " + item.getClass().getName());

                    // 特别关注空白样板
                    if (field.getName().toLowerCase().contains("blank") ||
                            field.getName().toLowerCase().contains("pattern")) {
                        System.out.println("[ACL DEBUG]  ⭐ PATTERN ITEM: " + item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}