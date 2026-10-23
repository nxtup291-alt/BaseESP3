package com.baseesp.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class BaseEspSettingsScreen extends Screen {
    private int row;
    public BaseEspSettingsScreen() { super(Text.literal("BaseESP Settings")); }
    @Override protected void init() {
        row = 0;
        int left = this.width / 2 - 105;
        int top = 34;
        addToggle(left, top, "Storage containers", () -> BaseEspClient.CONFIG.storage, v -> BaseEspClient.CONFIG.storage = v);
        addToggle(left, top, "Hostile / other mobs", () -> BaseEspClient.CONFIG.mobs, v -> BaseEspClient.CONFIG.mobs = v);
        addToggle(left, top, "Villagers", () -> BaseEspClient.CONFIG.villagers, v -> BaseEspClient.CONFIG.villagers = v);
        addToggle(left, top, "Animals", () -> BaseEspClient.CONFIG.animals, v -> BaseEspClient.CONFIG.animals = v);
        addToggle(left, top, "Beds", () -> BaseEspClient.CONFIG.beds, v -> BaseEspClient.CONFIG.beds = v);
        addToggle(left, top, "Utility / work blocks", () -> BaseEspClient.CONFIG.utilityBlocks, v -> BaseEspClient.CONFIG.utilityBlocks = v);
        addToggle(left, top, "Doors", () -> BaseEspClient.CONFIG.doors, v -> BaseEspClient.CONFIG.doors = v);
        addToggle(left, top, "Signs", () -> BaseEspClient.CONFIG.signs, v -> BaseEspClient.CONFIG.signs = v);
        addToggle(left, top, "HUD overlay", () -> BaseEspClient.CONFIG.showHud, v -> BaseEspClient.CONFIG.showHud = v);
        addToggle(left, top, "Base rating", () -> BaseEspClient.CONFIG.showBaseRating, v -> BaseEspClient.CONFIG.showBaseRating = v);
        addToggle(left, top, "Known container contents", () -> BaseEspClient.CONFIG.showContentsWhenKnown, v -> BaseEspClient.CONFIG.showContentsWhenKnown = v);
        addButton(left, top, "Scan size: " + (BaseEspClient.CONFIG.radius * 2) + "x" + (BaseEspClient.CONFIG.radius * 2), () -> {
            BaseEspClient.CONFIG.radius = BaseEspClient.CONFIG.radius >= 20 ? 5 : BaseEspClient.CONFIG.radius + 5;
            BaseEspClient.CONFIG.save(); clearAndInit();
        });
        addButton(left, top, "Save and close", () -> { BaseEspClient.CONFIG.save(); close(); });
    }
    private interface Getter { boolean get(); }
    private interface Setter { void set(boolean value); }
    private void addToggle(int left, int top, String name, Getter getter, Setter setter) {
        int y = top + row++ * 22;
        addDrawableChild(ButtonWidget.builder(Text.literal(name + ": " + (getter.get() ? "ON" : "OFF")), b -> {
            setter.set(!getter.get()); BaseEspClient.CONFIG.save(); clearAndInit();
        }).dimensions(left, y, 210, 20).build());
    }
    private void addButton(int left, int top, String name, Runnable action) {
        int y = top + row++ * 22;
        addDrawableChild(ButtonWidget.builder(Text.literal(name), b -> action.run()).dimensions(left, y, 210, 20).build());
    }
    private void clearAndInit() { clearChildren(); init(); }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
    @Override public void close() { BaseEspClient.CONFIG.save(); if (client != null) client.setScreen(null); }
}
