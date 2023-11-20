package com.lumintorious.tfcambiental.api;

import com.lumintorious.tfcambiental.modifier.TempModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;

import java.util.Optional;
import java.util.Set;

@FunctionalInterface
public interface EnvironmentalTemperatureProvider
{
    Optional<TempModifier> getModifier(Player player);

    static boolean calculateEnclosure(Player player, int radius) {
        PathNavigationRegion region = new PathNavigationRegion(
                player.level(),
                player.getOnPos().above().offset(-radius, -radius, -radius),
                player.getOnPos().above().offset(radius, 400, radius)
        );
        Bee guineaPig = new Bee(EntityType.BEE, player.level());
        guineaPig.setPos(player.getPosition(0));
        guineaPig.setBaby(true);
        FlyNodeEvaluator evaluator = new FlyNodeEvaluator();
        PathFinder finder = new PathFinder(evaluator, 500);
        Path path =
                finder
                        .findPath(
                                region,
                                guineaPig,
                                Set.of(player.getOnPos().above().atY(258)),
                                500,
                                0,
                                12
                        );
        boolean isIndoors = path == null || path.getNodeCount() < 255 - player.getOnPos().above().getY();
        return isIndoors;
    }
}
