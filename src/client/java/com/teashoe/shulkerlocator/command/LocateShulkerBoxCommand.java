package com.teashoe.shulkerlocator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.teashoe.shulkerlocator.ShulkerBoxTracker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;

import java.util.*;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class LocateShulkerBoxCommand {

    public static void registerClient(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("shulkerlocate")
                        .executes(LocateShulkerBoxCommand::executeLocate)
        );
    }

    private static int executeLocate(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MinecraftClient client = source.getClient();

        if (client.world == null || client.player == null) {
            source.sendFeedback(Text.literal("World not loaded.")
                    .formatted(Formatting.RED));
            return 0;
        }

        ShulkerBoxTracker tracker = ShulkerBoxTracker.getInstance();
        List<ShulkerBoxTracker.ShulkerBoxData> allShulkerBoxes = tracker.getAllShulkerBoxes();

        if (allShulkerBoxes.isEmpty()) {
            source.sendFeedback(Text.literal("No shulker boxes found.")
                    .formatted(Formatting.YELLOW));
            return 0;
        }

        // 차원별로 그룹화
        Map<String, List<ShulkerBoxTracker.ShulkerBoxData>> dimensionGroups = new LinkedHashMap<>();
        for (ShulkerBoxTracker.ShulkerBoxData data : allShulkerBoxes) {
            dimensionGroups.computeIfAbsent(data.dimensionId, k -> new ArrayList<>()).add(data);
        }

        source.sendFeedback(Text.literal("===== Shulker Box Locations =====")
                .formatted(Formatting.GREEN));

        // 차원별로 출력
        boolean isFirstDimension = true;
        for (Map.Entry<String, List<ShulkerBoxTracker.ShulkerBoxData>> entry : dimensionGroups.entrySet()) {
            String dimensionId = entry.getKey();
            List<ShulkerBoxTracker.ShulkerBoxData> boxes = entry.getValue();

            // 첫 번째 차원이 아니면 빈 줄 추가
            if (!isFirstDimension) {
                source.sendFeedback(Text.literal(""));
            }
            isFirstDimension = false;

            // 차원 이름 및 색상 결정
            String dimensionName = getDimensionDisplayName(dimensionId);
            Formatting dimensionColor = getDimensionColor(dimensionId);

            source.sendFeedback(Text.literal("[" + dimensionName + "]")
                    .formatted(dimensionColor));

            // 해당 차원의 셜커박스들 출력
            for (ShulkerBoxTracker.ShulkerBoxData data : boxes) {
                int x = data.pos.getX();
                int y = data.pos.getY();
                int z = data.pos.getZ();

                DyeColor dyeColor = getDyeColorFromString(data.color);
                Formatting colorFormat = getFormattingForColor(dyeColor);

                // 이름이 있으면 그대로, 없으면 Unnamed 표시
                String displayName = (data.name != null && !data.name.isEmpty())
                        ? data.name
                        : "Unnamed";

                source.sendFeedback(Text.literal(
                        String.format("  %s : [%d, %d, %d]", displayName, x, y, z)
                ).formatted(colorFormat));
            }
        }

        source.sendFeedback(Text.literal(
                String.format("Found %d shulker box%s.", allShulkerBoxes.size(), allShulkerBoxes.size() == 1 ? "" : "es")
        ).formatted(Formatting.GREEN));

        return allShulkerBoxes.size();
    }

    private static String getDimensionDisplayName(String dimensionId) {
        if (dimensionId.contains("overworld")) {
            return "Overworld";
        } else if (dimensionId.contains("the_nether")) {
            return "The Nether";
        } else if (dimensionId.contains("the_end")) {
            return "The End";
        }
        return dimensionId; // 알 수 없는 차원은 ID 그대로 표시
    }

    private static Formatting getDimensionColor(String dimensionId) {
        if (dimensionId.contains("overworld")) {
            return Formatting.DARK_GREEN;
        } else if (dimensionId.contains("the_nether")) {
            return Formatting.DARK_RED;
        } else if (dimensionId.contains("the_end")) {
            return Formatting.WHITE;
        }
        return Formatting.GRAY; // 알 수 없는 차원
    }

    private static Formatting getFormattingForColor(DyeColor color) {
        return switch (color) {
            case WHITE -> Formatting.WHITE;
            case ORANGE -> Formatting.GOLD;
            case MAGENTA -> Formatting.LIGHT_PURPLE;
            case LIGHT_BLUE -> Formatting.AQUA;
            case YELLOW -> Formatting.YELLOW;
            case LIME -> Formatting.GREEN;
            case PINK -> Formatting.LIGHT_PURPLE;
            case GRAY -> Formatting.DARK_GRAY;
            case LIGHT_GRAY -> Formatting.GRAY;
            case CYAN -> Formatting.DARK_AQUA;
            case PURPLE -> Formatting.DARK_PURPLE;
            case BLUE -> Formatting.BLUE;
            case BROWN -> Formatting.GOLD;
            case GREEN -> Formatting.DARK_GREEN;
            case RED -> Formatting.RED;
            case BLACK -> Formatting.DARK_GRAY;
        };
    }

    private static DyeColor getDyeColorFromString(String colorName) {
        if (colorName == null || colorName.isEmpty()) {
            return DyeColor.PURPLE;
        }

        try {
            return DyeColor.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DyeColor.PURPLE; // 기본값
        }
    }
}