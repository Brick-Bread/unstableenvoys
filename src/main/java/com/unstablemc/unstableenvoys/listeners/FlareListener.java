package com.unstablemc.unstableenvoys.listeners;

import com.artillexstudios.axapi.utils.Cooldown;
import com.artillexstudios.axapi.utils.StringUtils;
import com.unstablemc.unstableenvoys.UnstableEnvoysPlugin;
import com.unstablemc.unstableenvoys.envoy.Envoy;
import com.unstablemc.unstableenvoys.envoy.Envoys;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public class FlareListener implements Listener {
    public static final NamespacedKey KEY = new NamespacedKey(UnstableEnvoysPlugin.getInstance(), "unstableenvoys_flare");
    private static final Cooldown<UUID> flareCooldown = Cooldown.create();

    @EventHandler
    private void onPlayerInteractEvent(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem() == null) return;
        final ItemMeta meta = event.getItem().getItemMeta();
        if (meta == null) return;
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(KEY, PersistentDataType.STRING)) return;

        String envoyName = container.get(KEY, PersistentDataType.STRING);
        if (envoyName == null) return;
        Envoy envoy = Envoys.valueOf(envoyName);
        if (envoy == null) return;

        if (!envoy.getConfig().FLARE_ENABLED) {
            event.getPlayer().sendMessage(StringUtils.formatToString(envoy.getConfig().PREFIX + envoy.getConfig().FLARE_DISABLED));
            return;
        }

        if (envoy.isActive()) {
            event.getPlayer().sendMessage(StringUtils.formatToString(envoy.getConfig().PREFIX + envoy.getConfig().ALREADY_ACTIVE));
            return;
        }

        if (envoy.getCenter() == null) {
            return;
        }

        if (flareCooldown.hasCooldown(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(StringUtils.formatToString(envoy.getConfig().PREFIX + envoy.getConfig().FLARE_COOLDOWN_MESSAGE));
            return;
        }

        if (envoy.start(event.getPlayer())) {
            flareCooldown.addCooldown(event.getPlayer().getUniqueId(), Duration.ofSeconds(envoy.getConfig().FLARE_COOLDOWN).toMillis());
            if (event.getItem().getAmount() > 1) {
                event.getItem().setAmount(event.getItem().getAmount() - 1);
            } else {
                event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }
        }
    }
}
