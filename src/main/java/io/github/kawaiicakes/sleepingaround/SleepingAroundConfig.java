package io.github.kawaiicakes.sleepingaround;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class SleepingAroundConfig {
    protected static ForgeConfigSpec SERVER_SPEC;
    protected static ConfigValues SERVER;

    static {
        Pair<ConfigValues, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ConfigValues::new);
        SERVER_SPEC = pair.getRight();
        SERVER = pair.getLeft();
    }

    public static class ConfigValues {
        public final ForgeConfigSpec.BooleanValue multipleSpawns, randomSpawn, randomWorldspawn;
        public final ForgeConfigSpec.IntValue maxSpawnsPerDim, maxSpawns;
        public final ForgeConfigSpec.DoubleValue randomSpawnRadius;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> dimBlacklist;

        protected ConfigValues(ForgeConfigSpec.Builder builder) {
            builder.push("Multiple Spawnpoints").translation(key("sp_header"));

            this.multipleSpawns = builder
                    .comment("Whether players may have multiple spawnpoints.")
                    .translation(key("multiple_spawnpoints"))
                    .define("multiple_spawnpoints", true);

            this.randomSpawn = builder
                    .comment("Whether players will randomly spawn at one of their spawnpoints.")
                    .translation(key("random_spawns"))
                    .define("random_spawns", false);

            this.maxSpawns = builder
                    .comment("The maximum number of spawnpoints allowed per player. Set to 0 to disable this limit.")
                    .translation(key("max_spawnpoints"))
                    .defineInRange("max_spawnpoints", 0, 0, Integer.MAX_VALUE);

            this.maxSpawnsPerDim = builder
                    .comment("The maximum number of spawnpoints allowed for a player per dimension. Set to 0 to disable this limit.")
                    .translation(key("max_spawnpoints_per-dimension"))
                    .defineInRange("max_spawnpoints_per-dimension", 0, 0, Integer.MAX_VALUE);

            this.dimBlacklist = builder
                    .comment("Place the ID of any dimension in this list if you wish to disable multiple spawnpoints there.")
                    .translation(key("dimension_blacklist"))
                    .defineList("dimension_blacklist", ObjectArrayList.wrap(new String[]{}), (obj) ->
                            obj instanceof String string && ResourceLocation.isValidResourceLocation(string));

            builder.pop();
            builder.push("Random Worldspawn").translation(key("random_worldspawn"));

            this.randomWorldspawn = builder
                    .comment("Whether random worldspawn is turned on or not.")
                    .translation(key("random_worldspawns"))
                    .define("random_worldspawns", false);

            this.randomSpawnRadius = builder
                    .comment("The maximum distance from worldspawn that a player can spawn at randomly.")
                    .translation(key("random_worldspawn_radius"))
                    .defineInRange("random_worldspawn_radius", 10000D, 0D, 30000000D);

            builder.pop();
        }

        private static String key(String valueName) {
            return "config.sleepingaround." + valueName;
        }
    }
}
