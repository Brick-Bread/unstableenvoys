package com.unstablemc.unstableenvoys.config.impl;

import com.unstablemc.unstableenvoys.config.AbstractConfig;
import com.unstablemc.unstableenvoys.utils.FileUtils;

import java.util.List;

public class Config extends AbstractConfig {

    @Key("listen-to-block-physics")
    @Comment({"Enable this, if you want to prevent the crateTypes from changing their surrounding blocks.",
            "Examples:",
            "If a crateType spawns on a 'dirt path' block, that block will become dirt, if this feature is disabled.",
            "Warning: This will probably tank your performance at crateType spawning, if lots of crateTypes spawn (lots = hundreds)",
            "Changing this requires a restart!"
    })
    public static boolean LISTEN_TO_BLOCK_PHYSICS = false;

    @Key("dont-replace-blocks")
    @Comment({
            "Enable this, if you want to prevent some blocks that",
            "replaced by crates in certain cases."
    })
    public static boolean DONT_REPLACE_BLOCKS = true;

    @Key("passable-blocks")
    @Comment({
            "Add any blocks that you want to be passable",
            "when using the iterative block-finder. These",
            "blocks will not be used for determining the top block."
    })
    public static List<String> PASSABLE_BLOCKS = List.of("air", "barrier");

    @Key("debug")
    @Comment({
            "Enable this, if you'd like to receive some detailed",
            "logs of why something is happening. This will send messages",
            "in the console."
    })
    public static boolean DEBUG = false;

    private static final Config CONFIG = new Config();

    public static void reload() {
        FileUtils.extractFile(Config.class, "config.yml", FileUtils.PLUGIN_DIRECTORY, false);

        CONFIG.reload(FileUtils.PLUGIN_DIRECTORY.resolve("config.yml"), Config.class, null, null);
    }
}
