package com.teashoe.shulkerlocator.mixin;

import com.teashoe.shulkerlocator.ShulkerBoxTracker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class ClientTickMixin {
    private int tickCounter = 0;
    private static final int CHECK_INTERVAL = 40; // 40틱 = 2초
    private static final int CHECK_RANGE = 16; // 16블록 범위

    @Inject(method = "tick", at = @At("HEAD"))
    private void onClientTick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        // 플레이어와 월드가 존재하는지 확인
        if (client.player == null || client.world == null) {
            return;
        }

        tickCounter++;

        // 2초(40틱)마다 실행
        if (tickCounter >= CHECK_INTERVAL) {
            tickCounter = 0;
            validateNearbyShulkerBoxes(client.player, client.world);
        }
    }

    private void validateNearbyShulkerBoxes(ClientPlayerEntity player, World world) {
        ShulkerBoxTracker tracker = ShulkerBoxTracker.getInstance();
        BlockPos playerPos = player.getBlockPos();
        String dimensionId = world.getRegistryKey().getValue().toString();

        // 주변 16블록 내의 저장된 셜커박스 가져오기
        List<ShulkerBoxTracker.ShulkerBoxData> nearbyBoxes =
                tracker.getShulkerBoxesNearby(dimensionId, playerPos, CHECK_RANGE);

        for (ShulkerBoxTracker.ShulkerBoxData data : nearbyBoxes) {
            BlockState state = world.getBlockState(data.pos);

            // 해당 위치에 셜커박스가 없으면 삭제
            if (!(state.getBlock() instanceof ShulkerBoxBlock)) {
                String displayName = (data.name != null && !data.name.isEmpty())
                        ? data.name
                        : "Unnamed";

                tracker.removeShulkerBox(data.pos);

                player.sendMessage(
                        Text.literal("[" + displayName + "] was not detected and has been removed.")
                                .formatted(Formatting.YELLOW),
                        false
                );
            }
        }
    }
}