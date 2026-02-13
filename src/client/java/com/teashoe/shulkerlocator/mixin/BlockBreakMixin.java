package com.teashoe.shulkerlocator.mixin;

import com.teashoe.shulkerlocator.ShulkerBoxTracker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientWorld.class)
public class BlockBreakMixin {

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("HEAD"))
    private void onShulkerBoxBroken(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        ClientWorld world = (ClientWorld) (Object) this;
        BlockState oldState = world.getBlockState(pos);
        if (oldState.getBlock() instanceof ShulkerBoxBlock && !(state.getBlock() instanceof ShulkerBoxBlock)) {
            ShulkerBoxTracker.getInstance().removeShulkerBox(pos);
        }
    }
}
