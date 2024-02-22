package io.github.kawaiicakes.sleepingaround;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

import static io.github.kawaiicakes.sleepingaround.SleepingAround.MOD_ID;

/**
 * Capability storing spawn positions. This should be used whenever the player successfully sets their respawn somewhere.
 * Vice versa when removing spawnpoints on events like bed being obstructed/missing.
 */
public class SpawnPointsCapability {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation CAP_ID = new ResourceLocation(MOD_ID);

    /**
     * Convenience method for accessing this cap on a player.
     * @param player the <code>ServerPlayer</code> whose instance of this capability is to be accessed.
     * @return an <code>Optional</code> returning empty if this capability was unable to be obtained from the passed player.
     *          Returns this capability otherwise.
     */
    public static Optional<SpawnPointsCapability> getCapability(ServerPlayer player) {
        try {
            return player.getCapability(Provider.SP_CAP).resolve();
        } catch (Throwable e) {
            LOGGER.error("Error attempting to access capability of player {}!", player.getGameProfile().getName(), e);
            return Optional.empty();
        }
    }

    protected final Map<ResourceLocation, Set<BlockPos>> spawns = new HashMap<>();

    public boolean addSpawnpoint(ResourceKey<DimensionType> dimension, BlockPos spawnPos) {
        try {
            ResourceLocation dimensionId = dimension.location();
            Set<BlockPos> spawnPositions = this.spawns.computeIfAbsent(dimensionId, x -> new HashSet<>());
            return spawnPositions.add(spawnPos);
        } catch (Throwable e) {
            LOGGER.error("Error while trying to add spawnpoint!", e);
            return false;
        }
    }

    public boolean removeSpawnpoint(ResourceKey<DimensionType> dimension, BlockPos spawnPos) {
        try {
            ResourceLocation dimensionId = dimension.location();
            Set<BlockPos> spawnPositions = this.spawns.computeIfAbsent(dimensionId, x -> new HashSet<>());
            return spawnPositions.remove(spawnPos);
        } catch (Throwable e) {
            LOGGER.error("Error while trying to remove spawnpoint!", e);
            return false;
        }
    }

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof ServerPlayer player)) return;
        if ((player.getCapability(Provider.SP_CAP).isPresent())) return;

        event.addCapability(CAP_ID, new Provider());
    }

    public static class Provider implements ICapabilityProvider {
        public static Capability<SpawnPointsCapability> SP_CAP = CapabilityManager.get(new CapabilityToken<>() {});

        public SpawnPointsCapability capability;
        private final LazyOptional<SpawnPointsCapability> lazyHandler = LazyOptional.of(this::createCapability);

        public SpawnPointsCapability createCapability() {
            if (this.capability == null) {
                this.capability = new SpawnPointsCapability();
            }
            return this.capability;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == SP_CAP) {
                return lazyHandler.cast();
            }

            return LazyOptional.empty();
        }
    }
}
