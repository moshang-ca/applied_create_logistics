package com.moshang.appliedcreatelogistics.items.LogisticsPattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;

import java.util.*;

public class LogisticsNetworkHandler implements ICraftingProvider {
    private final Map<String, List<LogisticsPatternDetails>> addressPatterns = new HashMap<>();
    private IGridNode gridNode;
    private boolean initialized = false;

    public LogisticsNetworkHandler(IGridNode gridNode) {
        this.gridNode = gridNode;
        this.gridNode.getGrid().getCraftingService().refreshNodeCraftingProvider(this.gridNode);
    }

    public void registerPattern(LogisticsPatternDetails pattern) {
        String address = pattern.getDefaultAddress();
        this.addressPatterns.computeIfAbsent(address, k -> new ArrayList<>()).add(pattern);

        this.gridNode.getGrid().getCraftingService().refreshNodeCraftingProvider(this.gridNode);
    }

    public void unregisterPattern(LogisticsPatternDetails pattern) {
        String address = pattern.getDefaultAddress();
        List<LogisticsPatternDetails> patterns = this.addressPatterns.get(address);
        if(patterns != null) {
            patterns.remove(pattern);
            if(patterns.isEmpty()) {
                this.addressPatterns.remove(address);
            }

            this.gridNode.getGrid().getCraftingService().refreshNodeCraftingProvider(this.gridNode);
        }
    }

    public List<LogisticsPatternDetails> getPatternsByAddress(String address) {
        return this.addressPatterns.getOrDefault(address, new ArrayList<>());
    }

    public Set<String> getAvailableAddresses() {
        return this.addressPatterns.keySet();
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        List<IPatternDetails> allPatterns = new ArrayList<>();
        for(List<LogisticsPatternDetails> patterns : this.addressPatterns.values()) {
            allPatterns.addAll(patterns);
        }
        return allPatterns;
    }

    @Override
    public int getPatternPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] keyCounters) {
        if(patternDetails instanceof LogisticsPatternDetails logisticsPattern) {
            String address = logisticsPattern.getDefaultAddress();
            return true;
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }
}
