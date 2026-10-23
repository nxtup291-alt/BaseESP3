package com.baseesp.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BaseEspClient implements ClientModInitializer {
    public static BaseEspConfig CONFIG = BaseEspConfig.load();
    private static KeyBinding settingsKey;
    private static ScanResult latest = new ScanResult();
    private static int tickCounter;

    @Override public void onInitializeClient() {
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.baseesp.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.baseesp"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (settingsKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new BaseEspSettingsScreen());
            }
            if (++tickCounter % 10 == 0 && client.player != null && client.world != null) {
                latest = scan(client.world, client.player.getBlockPos());
            }
        });
        HudRenderCallback.EVENT.register(BaseEspClient::renderHud);
        WorldRenderEvents.AFTER_ENTITIES.register(context -> renderWorld(context.matrixStack(), context.consumers(), context.camera().getPos()));
        CONFIG.save();
    }

    private static ScanResult scan(World world, BlockPos center) {
        ScanResult out = new ScanResult();
        int r = CONFIG.radius;
        int minY = Math.max(world.getBottomY(), center.getY() - 4);
        int maxY = Math.min(world.getTopY() - 1, center.getY() + 4);
        for (int x = center.getX() - r; x < center.getX() + r; x++) {
            for (int z = center.getZ() - r; z < center.getZ() + r; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    if (CONFIG.storage && isStorage(block)) {
                        out.storage++;
                        out.storagePositions.add(pos.toImmutable());
                        out.add("Storage", labelFor(block));
                        if (CONFIG.showContentsWhenKnown) addKnownContents(world.getBlockEntity(pos), out, labelFor(block));
                    }
                    if (CONFIG.beds && (block == Blocks.RED_BED || block == Blocks.BLACK_BED || block == Blocks.BLUE_BED || block == Blocks.BROWN_BED || block == Blocks.CYAN_BED || block == Blocks.GRAY_BED || block == Blocks.GREEN_BED || block == Blocks.LIGHT_BLUE_BED || block == Blocks.LIGHT_GRAY_BED || block == Blocks.LIME_BED || block == Blocks.MAGENTA_BED || block == Blocks.ORANGE_BED || block == Blocks.PINK_BED || block == Blocks.PURPLE_BED || block == Blocks.YELLOW_BED || block == Blocks.WHITE_BED)) {
                        out.beds++; out.add("Beds", "Bed");
                    }
                    if (CONFIG.utilityBlocks && isUtility(block)) { out.utility++; out.add("Utilities", labelFor(block)); }
                    if (CONFIG.doors && isDoor(block)) { out.doors++; out.add("Doors", labelFor(block)); }
                    if (CONFIG.signs && isSign(block)) { out.signs++; out.add("Signs", labelFor(block)); }
                }
            }
        }
        Box area = new Box(center.getX() - r, minY, center.getZ() - r, center.getX() + r, maxY + 1, center.getZ() + r);
        for (Entity entity : world.getOtherEntities(null, area, e -> e.isAlive())) {
            if (entity instanceof VillagerEntity && CONFIG.villagers) { out.villagers++; out.add("Villagers", "Villager"); }
            else if (entity instanceof AnimalEntity && CONFIG.animals) { out.animals++; out.add("Animals", entity.getType().getName().getString()); }
            else if (entity instanceof MobEntity && CONFIG.mobs) { out.mobs++; out.add("Mobs", entity.getType().getName().getString()); }
        }
        out.score = Math.min(100, out.storage * 9 + out.utility * 3 + out.beds * 18 + out.doors * 2 + out.villagers * 8 + out.animals * 2 + out.signs * 2);
        out.rating = out.score >= 70 ? "Very likely base" : out.score >= 40 ? "Likely base" : out.score >= 20 ? "Possible base" : "Low evidence";
        return out;
    }

    private static boolean isStorage(Block b) {
        return b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST || b == Blocks.BARREL || b == Blocks.ENDER_CHEST
                || b == Blocks.HOPPER || b == Blocks.DISPENSER || b == Blocks.DROPPER
                || b == Blocks.FURNACE || b == Blocks.BLAST_FURNACE || b == Blocks.SMOKER
                || b == Blocks.SHULKER_BOX || b == Blocks.WHITE_SHULKER_BOX || b == Blocks.ORANGE_SHULKER_BOX
                || b == Blocks.MAGENTA_SHULKER_BOX || b == Blocks.LIGHT_BLUE_SHULKER_BOX || b == Blocks.YELLOW_SHULKER_BOX
                || b == Blocks.LIME_SHULKER_BOX || b == Blocks.PINK_SHULKER_BOX || b == Blocks.GRAY_SHULKER_BOX
                || b == Blocks.LIGHT_GRAY_SHULKER_BOX || b == Blocks.CYAN_SHULKER_BOX || b == Blocks.PURPLE_SHULKER_BOX
                || b == Blocks.BLUE_SHULKER_BOX || b == Blocks.BROWN_SHULKER_BOX || b == Blocks.GREEN_SHULKER_BOX
                || b == Blocks.RED_SHULKER_BOX || b == Blocks.BLACK_SHULKER_BOX;
    }
    private static boolean isUtility(Block b) {
        return b == Blocks.CRAFTING_TABLE || b == Blocks.ENCHANTING_TABLE || b == Blocks.ANVIL || b == Blocks.CHIPPED_ANVIL
                || b == Blocks.DAMAGED_ANVIL || b == Blocks.BREWING_STAND || b == Blocks.SMITHING_TABLE
                || b == Blocks.GRINDSTONE || b == Blocks.STONECUTTER || b == Blocks.LOOM || b == Blocks.CARTOGRAPHY_TABLE
                || b == Blocks.FLETCHING_TABLE || b == Blocks.LECTERN || b == Blocks.BELL || b == Blocks.BEACON;
    }
    private static boolean isDoor(Block b) { return Registries.BLOCK.getId(b).getPath().endsWith("_door") || b == Blocks.IRON_DOOR; }
    private static boolean isSign(Block b) { String p = Registries.BLOCK.getId(b).getPath(); return p.endsWith("_sign") || p.endsWith("_hanging_sign"); }
    private static String labelFor(Block b) { return Registries.BLOCK.getId(b).getPath().replace('_', ' '); }

    private static void addKnownContents(BlockEntity be, ScanResult out, String container) {
        if (!(be instanceof Inventory inventory)) return;
        List<String> contents = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) contents.add(stack.getCount() + "x " + stack.getName().getString());
        }
        if (!contents.isEmpty()) out.contents.put(container + " contents", String.join(", ", contents));
    }

    private static void renderWorld(net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider consumers, Vec3d camera) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
        matrices.push(); matrices.translate(-camera.x, -camera.y, -camera.z);
        if (CONFIG.storage) {
            for (BlockPos pos : latest.storagePositions) {
                Box b = new Box(pos).expand(0.003);
                net.minecraft.client.render.WorldRenderer.drawBox(matrices, lines, b, 0.15f, 0.95f, 0.45f, 0.95f);
            }
        }
        BlockPos p = client.player.getBlockPos(); int r = CONFIG.radius;
        Box square = new Box(p.getX() - r, p.getY() - 0.98, p.getZ() - r, p.getX() + r, p.getY() - 0.94, p.getZ() + r);
        net.minecraft.client.render.WorldRenderer.drawBox(matrices, lines, square, 0.25f, 0.75f, 1.0f, 0.65f);
        matrices.pop();
    }

    private static void renderHud(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!CONFIG.showHud || client.player == null || client.world == null) return;
        int x = 8, y = 8;
        ctx.drawTextWithShadow(client.textRenderer, Text.literal("§aBaseESP §7( H: settings )"), x, y, 0xFFFFFF); y += 12;
        ctx.drawTextWithShadow(client.textRenderer, Text.literal("20x20 area · storage: " + latest.storage), x, y, 0xFFFFFF); y += 12;
        if (CONFIG.showBaseRating) { ctx.drawTextWithShadow(client.textRenderer, Text.literal("Base estimate: " + latest.score + "% — " + latest.rating), x, y, 0xFFFFFF); y += 12; }
        String entities = "Mobs " + latest.mobs + " | Villagers " + latest.villagers + " | Animals " + latest.animals;
        ctx.drawTextWithShadow(client.textRenderer, Text.literal(entities), x, y, 0xD8D8D8); y += 12;
        for (Map.Entry<String, Integer> e : latest.counts.entrySet()) {
            if (y > ctx.getScaledWindowHeight() - 28) break;
            ctx.drawTextWithShadow(client.textRenderer, Text.literal(e.getKey() + ": " + e.getValue()), x, y, 0xD8D8D8); y += 11;
        }
        for (Map.Entry<String, String> e : latest.contents.entrySet()) {
            if (y > ctx.getScaledWindowHeight() - 28) break;
            String line = e.getKey() + ": " + e.getValue();
            if (line.length() > 90) line = line.substring(0, 87) + "...";
            ctx.drawTextWithShadow(client.textRenderer, Text.literal(line), x, y, 0xB9E7FF); y += 11;
        }
        if (latest.storage > 0 && latest.contents.isEmpty() && CONFIG.showContentsWhenKnown && y < ctx.getScaledWindowHeight() - 12)
            ctx.drawTextWithShadow(client.textRenderer, Text.literal("Container contents unavailable until server exposes them."), x, y, 0xAAAAAA);
    }

    static final class ScanResult {
        int storage, beds, utility, doors, signs, mobs, villagers, animals, score;
        String rating = "Low evidence";
        final List<BlockPos> storagePositions = new ArrayList<>();
        final Map<String, Integer> counts = new LinkedHashMap<>();
        final Map<String, String> contents = new LinkedHashMap<>();
        void add(String category, String name) { counts.merge(category, 1, Integer::sum); }
    }
}
