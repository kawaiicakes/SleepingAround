package io.github.kawaiicakes.sleepingaround;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SleepingAround.MODID)
public class SleepingAround {
    public static final String MODID = "sleepingaround";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SleepingAround()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
    }
}
