package com.moshang.appliedcreatelogistics.mechanicalProvider;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MechanicalLogisticsProviderScreen extends AbstractContainerScreen<MechanicalLogisticsProviderMenu> {
    private static final ResourceLocation GUI_TEXTURES =
            ResourceLocation.fromNamespaceAndPath("applied_create_logistics", "textures/guis/me_mechanical_logistics_provider_menu.png");

    public MechanicalLogisticsProviderScreen(MechanicalLogisticsProviderMenu menu, Inventory inv, Component title) {
        super(menu,  inv, title);
        this.imageWidth = 176;
        this.imageHeight = 212;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int x = (this.width - imageWidth) / 2;
        int y = (this.height - imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURES, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, this.title.getString(), 8, 6, 4210752);
        guiGraphics.drawString(font, "Inventory", 8, 720, 4210752);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
