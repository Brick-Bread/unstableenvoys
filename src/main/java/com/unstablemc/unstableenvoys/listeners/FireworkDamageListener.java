package com.unstablemc.unstableenvoys.listeners;

import com.unstablemc.unstableenvoys.envoy.SpawnedCrate;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class FireworkDamageListener implements Listener {

    @EventHandler
    public void onFireworkDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Firework fw)) return;
        if (!fw.getPersistentDataContainer().has(SpawnedCrate.FIREWORK_KEY, PersistentDataType.BYTE)) return;

        event.setCancelled(true);
    }
}
