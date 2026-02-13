package com.teashoe.shulkerlocator.mixin;

import com.teashoe.shulkerlocator.ShulkerBoxTracker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ShulkerBoxBlock.class)
public class BlockPlaceMixin {

    @Inject(method = "getPlacementState", at = @At("RETURN"))
    private void onShulkerBoxPlaced(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() != null && context.getWorld().isClient()) {
            BlockPos pos = context.getBlockPos();
            String dimensionId = context.getWorld().getRegistryKey().getValue().toString();

            ShulkerBoxBlock block = (ShulkerBoxBlock) (Object) this;
            DyeColor color = block.getColor();
            String colorName = color != null ? color.asString() : "purple";

            // 아이템 스택에서 커스텀 이름 가져오기
            ItemStack stack = context.getStack();
            String customName = null;
            Text nameText = stack.getName();
            if (nameText != null) {
                customName = nameText.getString();
            }

            ShulkerBoxTracker.getInstance().addShulkerBox(pos, dimensionId, colorName, customName);
        }
    }
}