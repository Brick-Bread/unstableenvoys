package com.unstablemc.unstableenvoys.user;

import com.unstablemc.unstableenvoys.envoy.CrateType;
import com.unstablemc.unstableenvoys.envoy.Envoy;

public final class CrateCooldown {
    public Envoy envoy;
    public CrateType crateType;
    public long end;

    public CrateCooldown(Envoy envoy, CrateType crateType, long end) {
        this.envoy = envoy;
        this.crateType = crateType;
        this.end = end;
    }
}
