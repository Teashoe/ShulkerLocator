package com.teashoe.shulkerlocator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.teashoe.shulkerlocator.ShulkerBoxTracker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class LocateShulkerBoxCommand {

    public static void registerClient(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            literal("locate")
                .then(literal("shulkerbox")
                    .executes(LocateShulkerBoxCommand::executeLocate)
                )
        );
    }

    private static int executeLocate(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MinecraftClient client = source.getClient();

        if (client.world == null || client.player == null) {
            source.sendFeedback(Text.literal("월드가 로드되지 않았습니다.")
                .formatted(Formatting.RED));
            return 0;
        }

        ShulkerBoxTracker tracker = ShulkerBoxTracker.getInstance();
        String currentDimension = client.world.getRegistryKey().getValue().toString();

        List<ShulkerBoxTracker.ShulkerBoxData> shulkerBoxes = tracker.getShulkerBoxesInDimension(currentDimension);

        if (shulkerBoxes.isEmpty()) {
            source.sendFeedback(Text.literal("현재 차원에 설치한 셜커박스가 없습니다.")
                .formatted(Formatting.YELLOW));
            return 0;
        }

        source.sendFeedback(Text.literal("=== 셜커박스 위치 ===")
            .formatted(Formatting.GREEN));

        for (ShulkerBoxTracker.ShulkerBoxData data : shulkerBoxes) {
            int x = data.pos.getX();
            int y = data.pos.getY();
            int z = data.pos.getZ();

            double distance = Math.sqrt(
                Math.pow(x - client.player.getX(), 2) +
                Math.pow(y - client.player.getY(), 2) +
                Math.pow(z - client.player.getZ(), 2)
            );

            String distanceStr = String.format("%.1f", distance);

            source.sendFeedback(Text.literal(
                String.format("좌표: [%d, %d, %d] (거리: %s 블록)", x, y, z, distanceStr)
            ).formatted(Formatting.AQUA));
        }

        source.sendFeedback(Text.literal(
            String.format("총 %d개의 셜커박스를 찾았습니다.", shulkerBoxes.size())
        ).formatted(Formatting.GREEN));

        return shulkerBoxes.size();
    }
}
