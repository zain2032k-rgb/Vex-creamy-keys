package com.vexcreamy.keys;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class VexCreamyKeys implements ClientModInitializer {
    private static final int CREAM = 0xFFF9F0;
    private static final int PINK = 0xFFFFDDE8;
    private static final int PINK_ACTIVE = 0xFFFFBFD4;
    private static final int BROWN = 0xFF76535C;
    private static final Set<Integer> held = new HashSet<>();
    private static int leftCps, rightCps, leftUntil, rightUntil;
    private static long cpsWindow;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;
            long window = client.getWindow().getWindow();
            Set<Integer> now = new HashSet<>();
            for (int key = 32; key <= 348; key++) {
                if (GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS) now.add(key);
            }
            for (int key : now) {
                if (!held.contains(key) && client.player != null) {
                    client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.65F));
                }
            }
            held.clear();
            held.addAll(now);

            long time = System.currentTimeMillis();
            if (time - cpsWindow >= 1000) {
                leftCps = 0;
                rightCps = 0;
                cpsWindow = time;
            }
            if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                // Count on the rising edge, not every tick.
                if (!leftHeld) { leftCps++; leftUntil = 8; }
                leftHeld = true;
            } else leftHeld = false;
            if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS) {
                if (!rightHeld) { rightCps++; rightUntil = 8; }
                rightHeld = true;
            } else rightHeld = false;
            if (leftUntil > 0) leftUntil--;
            if (rightUntil > 0) rightUntil--;
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> renderHud(drawContext));
    }

    private static boolean leftHeld, rightHeld;

    private void renderHud(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        int x = mc.getWindow().getGuiScaledWidth() - 112;
        int y = 12;
        // Soft shadow and rounded-looking layered panel.
        g.fill(x - 5, y - 5, x + 105, y + 105, 0x553A2630);
        g.fill(x - 4, y - 4, x + 104, y + 104, CREAM);
        g.fill(x - 2, y - 2, x + 102, y + 102, 0xFFFFF8F5);
        g.drawCenteredString(mc.font, "VEXCREAMY", x + 50, y + 1, BROWN);

        int kx = x + 30;
        key(g, mc, kx, y + 16, "W", GLFW.GLFW_KEY_W);
        key(g, mc, kx - 28, y + 42, "A", GLFW.GLFW_KEY_A);
        key(g, mc, kx, y + 42, "S", GLFW.GLFW_KEY_S);
        key(g, mc, kx + 28, y + 42, "D", GLFW.GLFW_KEY_D);
        key(g, mc, x + 4, y + 68, "SHIFT", GLFW.GLFW_KEY_LEFT_SHIFT, 46);
        key(g, mc, x + 54, y + 68, "SPACE", GLFW.GLFW_KEY_SPACE, 46);
        g.drawString(mc.font, "LMB " + leftCps + " CPS", x + 3, y + 88, BROWN, false);
        g.drawString(mc.font, "RMB " + rightCps + " CPS", x + 57, y + 88, BROWN, false);
    }

    private void key(GuiGraphics g, Minecraft mc, int x, int y, String label, int code) {
        key(g, mc, x, y, label, code, 24);
    }

    private void key(GuiGraphics g, Minecraft mc, int x, int y, String label, int code, int width) {
        boolean active = held.contains(code);
        int fill = active ? PINK_ACTIVE : PINK;
        g.fill(x, y, x + width, y + 22, 0xFFE8C8D3);
        g.fill(x + 1, y + 1, x + width - 1, y + 21, fill);
        g.drawCenteredString(mc.font, label, x + width / 2, y + 7, BROWN);
    }
}
