package com.lumintorious.tfcambiental.api;

import com.lumintorious.tfcambiental.modifier.EnvironmentalModifier;
import com.lumintorious.tfcambiental.modifier.TempModifier;
import com.lumintorious.tfcambiental.modifier.TempModifierStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

@FunctionalInterface
public interface BlockTemperatureProvider
{
    Optional<TempModifier> getModifier(Player player, BlockPos pos, BlockState state);

    static void evaluateAll(Player player, TempModifierStorage storage) {
        BlockPos p = player.getOnPos();
        BlockPos pos1 = new BlockPos(p.getX() - 9, p.getY() - 3, p.getZ() - 9);
        BlockPos pos2 = new BlockPos(p.getX() + 9, p.getY() + 5, p.getZ() + 9);
        Iterable<BlockPos> allPositions = BlockPos.betweenClosed(pos1, pos2);
        BlockState skipState = Blocks.AIR.defaultBlockState();
        for (BlockPos pos : allPositions) {
            BlockState state = player.level().getBlockState(pos);
            if (state == skipState) {
                continue;
            }
//            if(state.getBlock() instanceof BlockRockVariant || state.getBlock() instanceof BlockRockRaw) {
//                continue;
//            }
            Block block = state.getBlock();
            double distance = Math.sqrt(player.getOnPos().distSqr(pos));
            float distanceMultiplier = (float) distance / 9f;
            distanceMultiplier = Math.min(1f, Math.max(0f, distanceMultiplier));
            distanceMultiplier = 1f - distanceMultiplier;
            boolean isInside = EnvironmentalModifier.getSkylight(player) < 14 && EnvironmentalModifier.getBlockLight(player) > 3;
            if (isInside) {
                distanceMultiplier *= 1.3f;
            }
            final float finalDistanceMultiplier = distanceMultiplier;
            for (BlockTemperatureProvider provider : AmbientalRegistry.BLOCKS) {
                //                    if(modifier.affectedByDistance){
                //                        modifier.setChange(modifier.getChange() * distanceMultiplier);
                //                        modifier.setPotency(modifier.getPotency() * distanceMultiplier);
                //                    }
                storage.add(provider.getModifier(player, pos, state));
            }
            BlockEntity entity = player.level().getBlockEntity(pos);
            if (entity != null) {
                for (BlockEntityTemperatureProvider provider : AmbientalRegistry.BLOCK_ENTITIES) {
                    provider.getModifier(player, entity).ifPresent((mod) -> {
                        mod.setChange(mod.getChange() * finalDistanceMultiplier);
                        mod.setPotency(mod.getPotency() * finalDistanceMultiplier);
                        storage.add(mod);
                    });
                }
            }
        }
    }
}
