package com.unstablemc.unstableenvoys.blockfinder;

import com.unstablemc.unstableenvoys.config.impl.EnvoyConfig;
import org.bukkit.Location;
import org.bukkit.block.Block;

public interface BlockFinder {

    Block highestBlockAt(Location location, EnvoyConfig config);
}
