package com.moshang.appliedcreatelogistics.api;

import appeng.api.networking.IGridNodeService;

public interface IPackagingProviderService extends IGridNodeService {
    boolean requestPackaging(String jobId, String destination);

    boolean isBusy();

    String getDefaultAddress();
}
