package com.unstablemc.unstableenvoys;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axapi.placeholders.PlaceholderHandler;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axapi.utils.mutable.MutableBoolean;
import com.unstablemc.unstableenvoys.commands.EnvoyCommand;
import com.unstablemc.unstableenvoys.config.impl.Config;
import com.unstablemc.unstableenvoys.config.impl.Messages;
import com.unstablemc.unstableenvoys.envoy.Crates;
import com.unstablemc.unstableenvoys.envoy.Envoy;
import com.unstablemc.unstableenvoys.envoy.Envoys;
import com.unstablemc.unstableenvoys.envoy.SpawnedCrate;
import com.unstablemc.unstableenvoys.libraries.Libraries;
import com.unstablemc.unstableenvoys.listeners.BlockPhysicsListener;
import com.unstablemc.unstableenvoys.listeners.CollectionListener;
import com.unstablemc.unstableenvoys.listeners.FireworkDamageListener;
import com.unstablemc.unstableenvoys.listeners.FlareListener;
import com.unstablemc.unstableenvoys.listeners.WorldLoadListener;
import com.unstablemc.unstableenvoys.placeholders.Placeholders;
import com.unstablemc.unstableenvoys.user.User;
import com.unstablemc.unstableenvoys.utils.EditorListener;
import com.unstablemc.unstableenvoys.utils.FallingBlockChecker;
import com.unstablemc.unstableenvoys.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class UnstableEnvoysPlugin extends AxPlugin {
    public static NamespacedKey MESSAGE_KEY;
    private static UnstableEnvoysPlugin instance;
    private static Messages MESSAGES;
    private boolean placeholderApi;
    private boolean worldGuard;

    public static UnstableEnvoysPlugin getInstance() {
        return instance;
    }

    public static Messages getMessages() {
        return MESSAGES;
    }

    @Override
    public void updateFlags() {
        FeatureFlags.PACKET_ENTITY_TRACKER_ENABLED.set(true);
        FeatureFlags.HOLOGRAM_UPDATE_TICKS.set(10L);
    }

    @Override
    public void dependencies(DependencyManagerWrapper manager) {
        for (Libraries value : Libraries.values()) {
            manager.dependency(value.library());
        }
    }

    @Override
    public void enable() {
        instance = this;
        MESSAGE_KEY = new NamespacedKey(this, "envoy_messages");

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Enabled PlaceholderAPI hook!");
            this.placeholderApi = true;
            new Placeholders().register();
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            getLogger().info("Enabled WorldGuard hook!");
            this.worldGuard = true;
        }

        MESSAGES = new Messages("messages.yml");
        reload();

        PlaceholderHandler.register("hits", ctx -> {
            SpawnedCrate crate = ctx.raw(SpawnedCrate.class);
            return String.valueOf(crate.getHealth());
        });

        BukkitCommandHandler handler = BukkitCommandHandler.create(this);

        handler.registerValueResolver(Envoy.class, context -> {
            String envoy = context.popForParameter();

            return Envoys.valueOf(envoy.toLowerCase(Locale.ENGLISH));
        });

        handler.getAutoCompleter().registerParameterSuggestions(Envoy.class, (args, sender, command) -> Envoys.getTypes().keySet());

        handler.register(new EnvoyCommand());

        Bukkit.getOnlinePlayers().forEach(User::new);
        User.listen();
        FallingBlockChecker.start();
        Bukkit.getPluginManager().registerEvents(new FlareListener(), this);

        if (Config.LISTEN_TO_BLOCK_PHYSICS) {
            Bukkit.getPluginManager().registerEvents(new BlockPhysicsListener(), this);
        }

        Bukkit.getPluginManager().registerEvents(new CollectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new FireworkDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new EditorListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldLoadListener(), this);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            MutableBoolean failedToStart = new MutableBoolean(false);
            Envoys.getTypes().forEach((string, envoy) -> {
                if (failedToStart.booleanValue()) {
                    return;
                }

                if (envoy.getConfig().EVERY.isBlank() && envoy.getConfig().TIMES.isEmpty()) {
                    return;
                }

                if (envoy.isActive()) {
                    return;
                }

                ZonedDateTime now = ZonedDateTime.now();

                Iterator<Calendar> iterator = envoy.getWarns().iterator();
                while (iterator.hasNext()) {
                    Calendar warn = iterator.next();
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(warn.getTimeInMillis()), ZoneId.systemDefault());

                    if (zonedDateTime.getHour() == now.getHour() && zonedDateTime.getMinute() == now.getMinute() && zonedDateTime.getSecond() == now.getSecond()) {
                        iterator.remove();
                        if (!envoy.getConfig().ALERT.isBlank()) {
                            Bukkit.broadcastMessage(StringUtils.formatToString(envoy.getConfig().ALERT.replace("%time%", Utils.fancyTime(envoy.getNext().getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), envoy))));
                        }
                    }
                }

                ZonedDateTime next = ZonedDateTime.ofInstant(Instant.ofEpochMilli(envoy.getNext().getTimeInMillis()), ZoneId.systemDefault());
                if (next.getHour() == now.getHour() && next.getMinute() == now.getMinute() && next.getSecond() == now.getSecond()) {
                    if (envoy.startAttempt()) {
                        return;
                    }

                    envoy.setStartAttempt(true);
                    if (Bukkit.getOnlinePlayers().size() < envoy.getConfig().MIN_PLAYERS) {
                        failedToStart.set(true);
                        envoy.updateNext();
                        Bukkit.broadcastMessage(StringUtils.formatToString(envoy.getConfig().NOT_ENOUGH_AUTO_START));
                        return;
                    }

                    if (envoy.isActive()) {
                        envoy.updateNext();
                        return;
                    }

                    Scheduler.get().run(task -> {
                        envoy.start(null);
                    });
                }
            });
        }, 0, 200, TimeUnit.MILLISECONDS);

        Scheduler.get().runTimer(task -> {
            Envoys.getTypes().forEach((name, envoy) -> {
                if (!envoy.isActive()) return;

                for (SpawnedCrate spawnedCrate : envoy.getSpawnedCrates()) {
                    if (spawnedCrate.getHandle().getConfig().FLARE_EVERY == 0 || !spawnedCrate.getHandle().getConfig().FLARE_ENABLED)
                        continue;
                    spawnedCrate.tickFlare();
                }
            });
        }, 1, 1);
    }

    @Override
    public void reload() {
        Config.reload();
        MESSAGES.reload();
        Crates.reload();
        Envoys.reload();
    }

    @Override
    public void disable() {
        for (Envoy envoy : Envoys.getTypes().values()) {
            if (!envoy.isActive()) continue;
            envoy.stop();
        }
    }

    public boolean isPlaceholderApi() {
        return this.placeholderApi;
    }

    public boolean isWorldGuard() {
        return worldGuard;
    }
}
