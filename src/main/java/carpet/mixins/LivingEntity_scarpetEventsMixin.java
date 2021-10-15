package carpet.mixins;

import carpet.fakes.EntityInterface;
import carpet.fakes.LivingEntityInterface;
import carpet.script.EntityEventsGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.jetbrains.annotations.Nullable;

import static carpet.script.CarpetEventServer.Event.PLAYER_DEALS_DAMAGE;
import static carpet.script.CarpetEventServer.Event.PLAYER_EFFECT_APPLIED;
import static carpet.script.CarpetEventServer.Event.PLAYER_EFFECT_UPGRADED;
import static carpet.script.CarpetEventServer.Event.PLAYER_EFFECT_REMOVED;


@Mixin(LivingEntity.class)
public abstract class LivingEntity_scarpetEventsMixin extends Entity implements LivingEntityInterface
{

    @Shadow protected abstract void jump();

    @Shadow protected boolean jumping;

    public LivingEntity_scarpetEventsMixin(EntityType<?> type, World world)
    {
        super(type, world);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeathCall(DamageSource damageSource_1, CallbackInfo ci)
    {
        ((EntityInterface)this).getEventContainer().onEvent(EntityEventsGroup.Event.ON_DEATH, damageSource_1.name);
    }

    @Inject(method = "applyDamage", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;applyArmorToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F",
            shift = At.Shift.BEFORE
    ))
    private void entityTakingDamage(DamageSource source, float amount, CallbackInfo ci)
    {
        ((EntityInterface)this).getEventContainer().onEvent(EntityEventsGroup.Event.ON_DAMAGE, amount, source);
        // this is not applicable since its not a playr for sure
        //if (entity instanceof ServerPlayerEntity && PLAYER_TAKES_DAMAGE.isNeeded())
        //{
        //    PLAYER_TAKES_DAMAGE.onDamage(entity, float_2, damageSource_1);
        //}
        if (source.getAttacker() instanceof ServerPlayerEntity && PLAYER_DEALS_DAMAGE.isNeeded())
        {
            PLAYER_DEALS_DAMAGE.onDamage(this, amount, source);
        }
    }

		@Inject(method = "onStatusEffectApplied", at = @At("HEAD"))
    private void onEffectApplied(StatusEffectInstance effect, @Nullable Entity source, CallbackInfo ci)
    {
        if (PLAYER_EFFECT_APPLIED.isNeeded())
        {
            PLAYER_EFFECT_APPLIED.onEffectApplied((ServerPlayerEntity)(Object)this, effect.getEffectType().getName().getString(), effect.getAmplifier(), effect.getDuration());
        }
    }

		@Inject(method = "onStatusEffectUpgraded", at = @At("HEAD"))
    private void onEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, @Nullable Entity source, CallbackInfo ci)
    {
        if (PLAYER_EFFECT_UPGRADED.isNeeded())
        {
            PLAYER_EFFECT_UPGRADED.onEffectUpgraded((ServerPlayerEntity)(Object)this, effect.getEffectType().getName().getString(), effect.getAmplifier(), effect.getDuration());
        }
    }

		@Inject(method = "onStatusEffectRemoved", at = @At("HEAD"))
    private void onEffectRemoved(StatusEffectInstance effect, CallbackInfo ci)
    {
        if (PLAYER_EFFECT_REMOVED.isNeeded())
        {
            PLAYER_EFFECT_REMOVED.onEffectRemoved((ServerPlayerEntity)(Object)this, effect.getEffectType().getName().getString(), effect.getAmplifier());
        }
    }

    @Override
    public void doJumpCM()
    {
        jump();
    }

    @Override
    public boolean isJumpingCM()
    {
        return jumping;
    }
}
