package com.moshang.appliedcreatelogistics.items.LogisticsPattern;

import appeng.api.networking.IGridNode;

import java.util.HashMap;
import java.util.Map;

public class LogisticsPatternManager {
    private static final Map<IGridNode, LogisticsNetworkHandler> NODE_HANDLERS = new HashMap<>();

    public static LogisticsNetworkHandler getOrCreateHandler(IGridNode node) {
        return NODE_HANDLERS.computeIfAbsent(node, k -> new LogisticsNetworkHandler(node));
    }

    public static void removeHandler(IGridNode node) {
        LogisticsNetworkHandler handler = NODE_HANDLERS.remove(node);
        if(handler != null) {}
    }

    public static void registerPatternToNode(IGridNode node, LogisticsPatternDetails pattern) {
        LogisticsNetworkHandler handler = getOrCreateHandler(node);
        handler.registerPattern(pattern);
    }
}
