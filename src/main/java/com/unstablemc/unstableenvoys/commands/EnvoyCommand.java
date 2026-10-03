package com.unstablemc.unstableenvoys.commands;

import com.artillexstudios.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axapi.utils.StringUtils;
import com.unstablemc.unstableenvoys.UnstableEnvoysPlugin;
import com.unstablemc.unstableenvoys.envoy.Envoy;
import com.unstablemc.unstableenvoys.envoy.Envoys;
import com.unstablemc.unstableenvoys.envoy.SpawnedCrate;
import com.unstablemc.unstableenvoys.user.User;
import com.unstablemc.unstableenvoys.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.math3.util.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.List;

@Command({"envoy", "unstableenvoys", "uenvoy", "envoys"})
public class EnvoyCommand {

    @Subcommand("flare")
    @CommandPermission("unstableenvoys.command.flare")
    public void flare(CommandSender sender, Envoy envoy, @Default("me") Player receiver, @Default("1") int amount) {
        if (envoy == null) {
            Utils.sendMessage(sender, UnstableEnvoysPlugin.getMessages().PREFIX, UnstableEnvoysPlugin.getMessages().NO_ENVOY_FOUND);
            return;
        }

        ItemStack item = envoy.getFlare(amount);
        receiver.getInventory().addItem(item);
    }

    @Subcommand("start")
    @CommandPermission("unstableenvoys.command.start")
    public void start(CommandSender sender, Envoy envoy) {
        if (envoy == null) {
            Utils.sendMessage(sender, UnstableEnvoysPlugin.getMessages().PREFIX, UnstableEnvoysPlugin.getMessages().NO_ENVOY_FOUND);
            return;
        }

        envoy.start(null);
    }

    @Subcommand("stop")
    @CommandPermission("unstableenvoys.command.stop")
    public void stop(CommandSender sender, Envoy envoy) {
        if (envoy == null) {
            Utils.sendMessage(sender, UnstableEnvoysPlugin.getMessages().PREFIX, UnstableEnvoysPlugin.getMessages().NO_ENVOY_FOUND);
            return;
        }

        if (!envoy.isActive()) return;
        envoy.stop();
    }

    @Subcommand("stopall")
    @CommandPermission("unstableenvoys.command.stopall")
    public void stopAll(CommandSender sender) {
        for (Envoy envoy : Envoys.getTypes().values()) {
            if (!envoy.isActive()) return;
            envoy.stop();
        }
    }

    @Subcommand("reload")
    @CommandPermission("unstableenvoys.command.reload")
    public void reload(CommandSender sender) {
        Utils.sendMessage(sender, UnstableEnvoysPlugin.getMessages().PREFIX, UnstableEnvoysPlugin.getMessages().RELOAD.replace("%time%", String.valueOf(UnstableEnvoysPlugin.getInstance().reloadWithTime())));
    }

    @Subcommand("center")
    @CommandPermission("unstableenvoys.command.center")
    public void center(Player sender, Envoy envoy) {
        if (envoy == null) {
            Utils.sendMessage(sender, UnstableEnvoysPlugin.getMessages().PREFIX, UnstableEnvoysPlugin.getMessages().NO_ENVOY_FOUND);
            return;
        }

        envoy.getConfig().getConfig().set("random-spawn.center", Utils.serializeLocation(sender.getLocation()));

        try {
            envoy.getConfig().getConfig().save();
            envoy.getConfig().reload();
            Utils.sendMessage(sender, envoy.getConfig().PREFIX, envoy.getConfig().SET_CENTER);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Subcommand("editor")
    @CommandPermission("unstableenvoys.command.editor")
    public void editor(Player sender, @Optional Envoy envoy) {
        User user = User.USER_MAP.get(sender.getUniqueId());
        Envoy editor = user.getEditor();

        if (envoy == null || envoy.equals(editor)) {
            user.setEditor(null);
            if (editor != null) {
                List<Location> locations = editor.getConfig().PREDEFINED_LOCATIONS.stream().map(Utils::deserializeLocation).toList();

                for (Location location : locations) {
                    sender.sendBlockChange(location, Material.AIR.createBlockData());
                }

                sender.getInventory().remove(new ItemStack(Material.DIAMOND_BLOCK, 1));
                Utils.sendMessage(sender, editor.getConfig().PREFIX, editor.getConfig().EDITOR_LEAVE);
            }
            return;
        }

        user.setEditor(envoy);
        List<Location> locations = envoy.getConfig().PREDEFINED_LOCATIONS.stream().map(Utils::deserializeLocation).toList();

        for (Location location : locations) {
            sender.sendBlockChange(location, Material.DIAMOND_BLOCK.createBlockData());
        }

        sender.getInventory().addItem(new ItemStack(Material.DIAMOND_BLOCK));

        Utils.sendMessage(sender, envoy.getConfig().PREFIX, envoy.getConfig().EDITOR_JOIN);
    }

    @Subcommand("coords")
    @CommandPermission("unstableenvoys.command.coords")
    public void coords(CommandSender sender, Envoy envoy) {
        if (envoy == null) {
            Utils.sendMessage(sender, UnstableEnvoysPlugin.getMessages().PREFIX, UnstableEnvoysPlugin.getMessages().NO_ENVOY_FOUND);
            return;
        }

        for (SpawnedCrate spawnedCrate : envoy.getSpawnedCrates()) {
            Location finish = spawnedCrate.getFinishLocation();
            if (sender instanceof Player player) {
                ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
                wrapper.message(MiniMessage.miniMessage().deserialize("<click:run_command:'/tp %x% %y% %z%'><hover:show_text:'<color:#7df0ff>Click to teleport!</color>'><white>Crate</white> %crate% %x% %y% %z%.</hover></click>"
                        .replace("%x%", String.valueOf(finish.getBlockX()))
                        .replace("%y%", String.valueOf(finish.getBlockY()))
                        .replace("%z%", String.valueOf(finish.getBlockZ()))
                        .replace("%crate%", String.valueOf(spawnedCrate.getHandle().getName()))
                ));
            } else {
                sender.sendMessage(StringUtils.formatToString("<click:run_command:'/tp %x% %y% %z%'><hover:show_text:'<color:#7df0ff>Click to teleport!</color>'><white>Crate</white> %crate% %x% %y% %z%.</hover></click>"
                        .replace("%x%", String.valueOf(finish.getBlockX()))
                        .replace("%y%", String.valueOf(finish.getBlockY()))
                        .replace("%z%", String.valueOf(finish.getBlockZ()))
                        .replace("%crate%", String.valueOf(spawnedCrate.getHandle().getName()))
                ));
            }
        }
    }

    @Subcommand("toggle")
    @CommandPermission("unstableenvoys.command.toggle")
    public void toggle(Player sender) {
        if (sender.getPersistentDataContainer().has(UnstableEnvoysPlugin.MESSAGE_KEY, PersistentDataType.BYTE)) {
            sender.getPersistentDataContainer().remove(UnstableEnvoysPlugin.MESSAGE_KEY);
            Utils.sendMessage(sender, UnstableEnvoysPlugin.getMessages().PREFIX, UnstableEnvoysPlugin.getMessages().TOGGLE_ON);
            return;
        }

        sender.getPersistentDataContainer().set(UnstableEnvoysPlugin.MESSAGE_KEY, PersistentDataType.BYTE, (byte) 0);
        Utils.sendMessage(sender, UnstableEnvoysPlugin.getMessages().PREFIX, UnstableEnvoysPlugin.getMessages().TOGGLE_OFF);
    }

    @Subcommand("time")
    @CommandPermission("unstableenvoys.command.time")
    public void time(CommandSender sender) {
        Pair<Envoy, Long> pair = Utils.getNextEnvoy();
        Utils.sendMessage(sender, pair.getFirst().getConfig().PREFIX, pair.getFirst().getConfig().START_TIME.replace("%time%", Utils.fancyTime(pair.getSecond(), pair.getFirst())).replace("%envoy%", pair.getFirst().getName()));
    }
}
