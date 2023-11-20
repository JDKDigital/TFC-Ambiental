package com.lumintorious.tfcambiental.capability;

import com.lumintorious.tfcambiental.AmbientalDamage;
import com.lumintorious.tfcambiental.TFCAmbiental;
import com.lumintorious.tfcambiental.TFCAmbientalConfig;
import com.lumintorious.tfcambiental.api.BlockEntityTemperatureProvider;
import com.lumintorious.tfcambiental.api.BlockTemperatureProvider;
import com.lumintorious.tfcambiental.api.EquipmentTemperatureProvider;
import com.lumintorious.tfcambiental.api.ItemTemperatureProvider;
import com.lumintorious.tfcambiental.item.ClothesItem;
import com.lumintorious.tfcambiental.modifier.EnvironmentalModifier;
import com.lumintorious.tfcambiental.modifier.TempModifierStorage;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
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

public class TemperatureCapability implements ICapabilitySerializable<CompoundTag>
{
    public static final TemperatureCapability DEFAULT = new TemperatureCapability(true);
    public static final Capability<TemperatureCapability> CAPABILITY = Helpers.capability(new CapabilityToken<>()
    {
    });
    public static final ResourceLocation KEY = Helpers.identifier("temperature");

    public boolean isDefault;
    private int tick = 0;
    private int damageTick = 0;
    private int durabilityTick = 0;
    private Player player;
    private float target = 15;
    private float potency = 0;
    public float bodyTemperature;

    public static final float BAD_MULTIPLIER = 0.001f;
    public static final float GOOD_MULTIPLIER = 0.002f;
    public static final float CHANGE_CAP = 7.5f;
    public static final float HIGH_CHANGE = 0.20f;

    public TemperatureCapability() {
        this(false);
    }

    public TemperatureCapability(boolean isDefault) {
        this.bodyTemperature = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
        this.isDefault = isDefault;
    }

    public float getChange() {
        float speed = getPotency() * 0.025f * TFCAmbientalConfig.COMMON.temperatureChangeSpeed.get().floatValue();
        float change = Math.min(CHANGE_CAP, Math.max(-CHANGE_CAP, getTargetTemperature() - this.bodyTemperature));
        float newTemp = this.bodyTemperature + change;
        float AVERAGE = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
        if ((this.bodyTemperature < AVERAGE && newTemp > this.bodyTemperature) || (this.bodyTemperature > AVERAGE && newTemp < this.bodyTemperature)) {
            speed *= GOOD_MULTIPLIER * TFCAmbientalConfig.COMMON.goodTemperatureChangeSpeed.get().floatValue();
        } else {
            speed *= BAD_MULTIPLIER * TFCAmbientalConfig.COMMON.badTemperatureChangeSpeed.get().floatValue();
        }
        return (change * speed);
    }

    public TempModifierStorage modifiers = new TempModifierStorage();


    public void clearModifiers() {
        this.modifiers = new TempModifierStorage();
    }

    public void evaluateModifiers() {
        this.clearModifiers();
        ItemTemperatureProvider.evaluateAll(this.player, this.modifiers);
        EnvironmentalModifier.evaluateAll(this.player, this.modifiers);
        BlockTemperatureProvider.evaluateAll(this.player, this.modifiers);
        BlockEntityTemperatureProvider.evaluateAll(this.player, this.modifiers);
        EquipmentTemperatureProvider.evaluateAll(this.player, this.modifiers);
        this.modifiers.keepOnlyNEach(3);

        this.target = this.modifiers.getTargetTemperature();
        this.potency = this.modifiers.getTotalPotency();

        if (this.target > this.bodyTemperature && this.bodyTemperature > TFCAmbientalConfig.COMMON.hotThreshold.get().floatValue()) {
            this.potency /= this.potency;
        }
        if (this.target < this.bodyTemperature && this.bodyTemperature < TFCAmbientalConfig.COMMON.coolThreshold.get().floatValue()) {
            this.potency /= this.potency;
        }

        this.potency = Math.max(1f, this.potency);
    }

    public float getTargetTemperature() {
        return this.target;
    }

    public float getPotency() {
        return this.potency;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public float getTemperature() {
        return this.bodyTemperature;
    }

    public void setTemperature(float newTemp) {
        bodyTemperature = newTemp;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CAPABILITY) {
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
        tag.putFloat("target", this.target);
        tag.putFloat("potency", this.potency);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.bodyTemperature = nbt.getFloat("temperature");
        this.target = nbt.getFloat("target");
        this.potency = nbt.getFloat("potency");
    }

    public void update() {
        boolean server = !this.player.level().isClientSide();
        if (server) {
            this.setTemperature(this.getTemperature() + this.getChange());
            float envTemp = EnvironmentalModifier.getEnvironmentTemperatureWithTimeOfDay(player);
            float COLD = TFCAmbientalConfig.COMMON.coolThreshold.get().floatValue();
            float HOT = TFCAmbientalConfig.COMMON.hotThreshold.get().floatValue();

            if (envTemp > HOT || envTemp < COLD) {
                if (this.durabilityTick <= 600) {
                    this.durabilityTick++;
                } else {
                    this.durabilityTick = 0;
                    CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(c -> {
                        for (int i = 0; i < c.getSlots(); i++) {
                            ItemStack stack = c.getStackInSlot(i);
                            if (stack.getItem() instanceof ClothesItem) {
                                stack.setDamageValue(stack.getDamageValue() + 1);
                                if (stack.getDamageValue() > stack.getMaxDamage()) {
                                    stack.setCount(0);
                                }
                            }
                        }
                    });
                }
            }

            if (tick <= 20) {
                tick++;
                return;
            } else {
                tick = 0;
                if (this.damageTick > 40) {
                    this.damageTick = 0;
                    if (this.getTemperature() > TFCAmbientalConfig.COMMON.burnThreshold.get().floatValue()) {
                        player.hurt(new DamageSource(player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(AmbientalDamage.HOT)), 4f);
                    } else if (this.getTemperature() < TFCAmbientalConfig.COMMON.freezeThreshold.get().floatValue()) {
                        player.hurt(new DamageSource(player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(AmbientalDamage.FREEZE)), 4f);
                    }
                    if (player.getFoodData() instanceof TFCFoodData stats) {
                        if (this.getTemperature() > TFCAmbientalConfig.COMMON.burnThreshold.get().floatValue()) {
                            stats.addThirst(-8);
                        } else if (this.getTemperature() < TFCAmbientalConfig.COMMON.freezeThreshold.get().floatValue()) {
                            stats.setFoodLevel(stats.getFoodLevel() - 1);
                        }
                    }
                } else {
                    this.damageTick++;
                }
            }
            this.evaluateModifiers();
            sync();
        }

    }

    public void sync() {
        if (this.player instanceof ServerPlayer player) {
            TemperaturePacket packet = new TemperaturePacket(serializeNBT());
            TFCAmbiental.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
}
