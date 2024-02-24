package io.github.kawaiicakes.sleepingaround;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

import static io.github.kawaiicakes.sleepingaround.SleepingAroundConfig.SERVER;
import static io.github.kawaiicakes.sleepingaround.SleepingAroundConfig.SERVER_SPEC;

@Mod(SleepingAround.MOD_ID)
public class SleepingAround {
    public static final String MOD_ID = "sleepingaround";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Component SET_SPAWN_MESSAGE = Component.translatable("sleep.sleepingaround.set").withStyle(ChatFormatting.GREEN);
    public static final Component REMOVE_SPAWN_MESSAGE = Component.translatable("sleep.sleepingaround.remove").withStyle(ChatFormatting.GREEN);
    public static final Component FAIL1 = Component.translatable("sleep.sleepingaround.fail1").withStyle(ChatFormatting.RED);
    public static final Component FAIL2 = Component.translatable("sleep.sleepingaround.fail2").withStyle(ChatFormatting.RED);
    public static final Component FAIL3 = Component.translatable("sleep.sleepingaround.fail3").withStyle(ChatFormatting.RED);
    public static final Component FAIL4 = Component.translatable("sleep.sleepingaround.fail4").withStyle(ChatFormatting.RED);
    public static Component remainingPosMsg(int usedSpawns, int totalSpawns) {
        return Component.translatable("sleep.sleepingaround.remaining_pos", usedSpawns, totalSpawns);
    }
    public static Component remainingPosDimMsg(int usedSpawns, int totalSpawns, ResourceKey<Level> dimensionId) {
        return Component.translatable("sleep.sleepingaround.remaining_pos_dim", usedSpawns, totalSpawns, dimensionId.location());
    }

    // TODO: implement random spawns, figure out the deal with the GUI (death screen only? commands?)
    public SleepingAround() {
        MinecraftForge.EVENT_BUS.register(SleepingAround.class);
        MinecraftForge.EVENT_BUS.register(SpawnPointsCapability.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }

    @SubscribeEvent
    public static void onSetPlayerSpawn(PlayerSetSpawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        BlockPos spawnPos = event.getNewSpawn();
        if (spawnPos == null) return;
        if (spawnPos.equals(player.getRespawnPosition()) && event.getSpawnLevel().equals(player.getRespawnDimension())) return;

        SpawnPointsCapability capability;
        int usedSpawns;
        int usedSpawnsPerDim;
        int maxSpawns;
        int maxSpawnsPerDim;
        try {
            if (!SERVER.multipleSpawns.get()) return;
            if (SERVER.dimBlacklist.get().contains(event.getSpawnLevel().location().toString())) return;
            capability = SpawnPointsCapability.spawnpointsOf(player).orElseThrow();
            usedSpawns = capability.usedSpawns();
            usedSpawnsPerDim = capability.usedSpawns(event.getSpawnLevel());
            maxSpawns = SERVER.maxSpawns.get();
            maxSpawnsPerDim = SERVER.maxSpawnsPerDim.get();
        } catch (Throwable e) {
            LOGGER.error("Exception encountered while attempting to set multiple spawnpoints!", e);
            player.sendSystemMessage(FAIL2);
            return;
        }

        if (!capability.addSpawnpoint(event.getSpawnLevel(), event.getNewSpawn())) return;
        player.sendSystemMessage(SET_SPAWN_MESSAGE);

        if (maxSpawnsPerDim != 0) {
            player.sendSystemMessage(remainingPosDimMsg(usedSpawnsPerDim, maxSpawnsPerDim, event.getSpawnLevel()));
            if (usedSpawnsPerDim >= maxSpawnsPerDim) {
                capability.removeSpawnpoint(event.getSpawnLevel());
                player.sendSystemMessage(FAIL1);
                return;
            }
        }
        if (maxSpawns != 0) {
            player.sendSystemMessage(remainingPosMsg(usedSpawns, maxSpawns));
            if (usedSpawns >= maxSpawns) {
                capability.removeSpawnpoint(event.getSpawnLevel());
                player.sendSystemMessage(FAIL1);
            }
        }
    }

    @SubscribeEvent
    public static void onBreakOwnBed(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!(event.getState().getBlock() instanceof BedBlock)) return;

        if (!removeSpawnpointAt(event.getPlayer().level.dimension(), player, event.getPos())) {
            Direction offsetDirection = BedBlock.getConnectedDirection(event.getState());
            removeSpawnpointAt(event.getPlayer().level.dimension(), player, event.getPos().relative(offsetDirection));
        }
    }

    // TODO: this should be called when: via command, via GUI
    // FIXME: being able to spawn at removed spawnpoints
    public static boolean removeSpawnpointAt(ResourceKey<Level> dimension, ServerPlayer player, BlockPos spawnPos) {
        if (!SERVER.multipleSpawns.get()) return false;
        if (SERVER.dimBlacklist.get().contains(dimension.location().toString())) return false;

        try {
            SpawnPointsCapability capability = SpawnPointsCapability.spawnpointsOf(player).orElseThrow();
            if (!capability.removeSpawnpoint(dimension, spawnPos)) {
                player.sendSystemMessage(FAIL4);
                return false;
            }
            player.sendSystemMessage(REMOVE_SPAWN_MESSAGE);
            return true;
        } catch (Throwable e) {
            LOGGER.error("Error attempting to remove spawnpoint at {} for player {} in {}!", spawnPos, player.getGameProfile().getName(), dimension.location(), e);
            player.sendSystemMessage(FAIL3);
            return false;
        }
    }
}
