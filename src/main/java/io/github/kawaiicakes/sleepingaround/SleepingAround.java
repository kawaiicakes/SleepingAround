package io.github.kawaiicakes.sleepingaround;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import static io.github.kawaiicakes.sleepingaround.SleepingAroundConfig.SERVER_SPEC;

@Mod(SleepingAround.MOD_ID)
public class SleepingAround {
    public static final String MOD_ID = "sleepingaround";

    public SleepingAround() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(SpawnPointsCapability.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }
}
