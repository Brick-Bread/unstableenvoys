package com.unstablemc.unstableenvoys.event;

import com.unstablemc.unstableenvoys.envoy.Envoy;
import com.unstablemc.unstableenvoys.envoy.SpawnedCrate;
import com.unstablemc.unstableenvoys.rewards.Reward;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EnvoyCrateCollectEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final Envoy envoy;
    private final SpawnedCrate crate;
    private final Reward reward;

    public EnvoyCrateCollectEvent(Player player, Envoy envoy, SpawnedCrate crate, Reward reward) {
        this.player = player;
        this.envoy = envoy;
        this.crate = crate;
        this.reward = reward;
    }

    public Envoy getEnvoy() {
        return envoy;
    }

    public SpawnedCrate getCrate() {
        return crate;
    }

    public Player getPlayer() {
        return player;
    }

    public Reward getReward() {
        return reward;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
