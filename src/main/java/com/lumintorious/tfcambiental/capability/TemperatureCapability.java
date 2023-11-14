package com.lumintorious.tfcambiental.capability;

import com.lumintorious.tfcambiental.AmbientalDamage;
import com.lumintorious.tfcambiental.TFCAmbiental;
import com.lumintorious.tfcambiental.TFCAmbientalConfig;
import com.lumintorious.tfcambiental.api.*;
import com.lumintorious.tfcambiental.item.ClothesItem;
import com.lumintorious.tfcambiental.modifier.EnvironmentalModifier;
import com.lumintorious.tfcambiental.modifier.TempModifier;
import com.lumintorious.tfcambiental.modifier.TempModifierStorage;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

public class TemperatureCapability implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<TemperatureCapability> CAPABILITY = Helpers.capability(new CapabilityToken<>() {});
    public static final ResourceLocation KEY = Helpers.identifier("temperature");

    private int tick = 0;
    private int damageTick = 0;
    private int durabilityTick = 0;
    private Player player;
    private float target = 15;
    private float potency = 0;

    public float bodyTemperature = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
    public static final float BAD_MULTIPLIER = 0.001f;
    public static final float GOOD_MULTIPLIER = 0.002f;
    public static final float CHANGE_CAP = 7.5f;
    public static final float HIGH_CHANGE = 0.20f;

    public float getChange() {
        float target = getTarget();
        float speed = getPotency() * 0.025f * TFCAmbientalConfig.COMMON.temperatureChangeSpeed.get().floatValue();
        float change = Math.min(CHANGE_CAP, Math.max(-CHANGE_CAP, target - bodyTemperature));
        float newTemp = bodyTemperature + change;
        boolean isRising = true;
        float AVERAGE = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
        if((bodyTemperature < AVERAGE && newTemp > bodyTemperature) || (bodyTemperature > AVERAGE && newTemp < bodyTemperature)) {
            speed *= GOOD_MULTIPLIER * TFCAmbientalConfig.COMMON.goodTemperatureChangeSpeed.get().floatValue();
        }else {
            speed *= BAD_MULTIPLIER * TFCAmbientalConfig.COMMON.badTemperatureChangeSpeed.get().floatValue();
        }
        return ((float)change * speed);
    }

    public TempModifierStorage modifiers = new TempModifierStorage();


    public void clearModifiers() {
        this.modifiers = new TempModifierStorage();
    }

    public void evaluateModifiers() {
        TempModifierStorage previousStorage = modifiers;
        this.clearModifiers();
        ItemTemperatureProvider.evaluateAll(player, modifiers);
        EnvironmentalModifier.evaluateAll(player, modifiers);
        BlockTemperatureProvider.evaluateAll(player, modifiers);
        BlockEntityTemperatureProvider.evaluateAll(player, modifiers);
        EquipmentTemperatureProvider.evaluateAll(player, modifiers);
        this.modifiers.keepOnlyNEach(3);

        target = modifiers.getTargetTemperature();
        potency = modifiers.getTotalPotency();

        if(target > bodyTemperature && bodyTemperature > TFCAmbientalConfig.COMMON.hotThreshold.get().floatValue()) {
            potency /= potency;
        }
        if(target < bodyTemperature && bodyTemperature < TFCAmbientalConfig.COMMON.coolThreshold.get().floatValue()) {
            potency /= potency;
        }

        potency = Math.max(1f, potency);

//        for (TempModifier current : previousStorage) {
//            if (!this.modifiers.contains(current.getUnlocalizedName())) {
//                player.sendMessage(new TextComponent("No longer " + current.getUnlocalizedName() + " @ " + current.getChange()), player.getUUID());
//            }
//        }
//
//        for (TempModifier current : modifiers) {
//            if (!previousStorage.contains(current.getUnlocalizedName())) {
//                player.sendMessage(new TextComponent("Started " + current.getUnlocalizedName() + " @ " + current.getChange()), player.getUUID());
//            }
//        }
    }

    public float getChangeSpeed() {
        return potency;
    }

    public float getTarget() {
        return target;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public float getPotency() {
        return potency;
    }

    public void setPotency(float potency) {
        this.potency = potency;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public float getTemperature() {
        return bodyTemperature;
    }

    public void setTemperature(float newTemp) {
        bodyTemperature = newTemp;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == CAPABILITY) {
            return LazyOptional.of(() -> (T) this);
        } else {
            return LazyOptional.empty();
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return ICapabilitySerializable.super.getCapability(cap);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("temperature", getTemperature());
        tag.putFloat("target", target);
        tag.putFloat("potency", potency);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.bodyTemperature = nbt.getFloat("temperature");
        this.target = nbt.getFloat("target");
        this.potency = nbt.getFloat("potency");
    }

    public void update() {
        boolean server = !player.level().isClientSide();
        if(server) {
            this.setTemperature(this.getTemperature() + this.getChange());// / TFCAmbientalConfig.GENERAL.tickInterval);
            float envTemp = EnvironmentalModifier.getEnvironmentTemperatureWithTimeOfDay(player);
            float COLD = TFCAmbientalConfig.COMMON.coolThreshold.get().floatValue();
            float HOT = TFCAmbientalConfig.COMMON.hotThreshold.get().floatValue();

            if(envTemp > HOT || envTemp < COLD) {
                if(durabilityTick <= 600) {
                    durabilityTick++;
                } else {
                    durabilityTick = 0;
                    CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(c -> {
                        for(int i = 0; i < c.getSlots(); i++) {
                            ItemStack stack = c.getStackInSlot(i);
                            if(stack.getItem() instanceof ClothesItem) {
                                stack.setDamageValue(stack.getDamageValue() + 1);
                                if(stack.getDamageValue() > stack.getMaxDamage()) {
                                    stack.setCount(0);
                                }
                            }
                        }
                    });
                }
            }

            if(tick <= 20) {//TFCAmbientalConfig.GENERAL.tickInterval) {
                tick++;
                return;
            }else {
                tick = 0;
                if(damageTick > 40) {
                    damageTick = 0;
//                    if(TFCAmbientalConfig.GENERAL.takeDamage) {
                        if(this.getTemperature() > TFCAmbientalConfig.COMMON.burnThreshold.get().floatValue()) {
                            player.hurt(AmbientalDamage.HEAT,  4f);
                        }else if (this.getTemperature() < TFCAmbientalConfig.COMMON.freezeThreshold.get().floatValue()){
                            player.hurt(AmbientalDamage.COLD, 4f);
                        }
//                    }
//                    if(TFCAmbientalConfig.GENERAL.loseHungerThirst) {
                        if(player.getFoodData() instanceof TFCFoodData stats) {
                            if(this.getTemperature() > TFCAmbientalConfig.COMMON.burnThreshold.get().floatValue()) {
                                stats.addThirst(-8);
                            }else if (this.getTemperature() < TFCAmbientalConfig.COMMON.freezeThreshold.get().floatValue()){
                                stats.setFoodLevel(stats.getFoodLevel() - 1);
                            }
                        }

//                    }

                }else {
                    damageTick++;
                }
            }
            this.evaluateModifiers();
            sync();
        }

    }

    public void sync() {
        if (player instanceof ServerPlayer player)
        {
            TemperaturePacket packet = new TemperaturePacket(serializeNBT());
            TFCAmbiental.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
}
