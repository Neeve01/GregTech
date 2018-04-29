package gregtech.common.metatileentities.electric.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.FluidContainerSlotWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.render.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class MetaTileEntityFluidHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IFluidTank> {

    private static final int[] INVENTORY_SIZES = {8000, 12000, 16000, 32000, 48000, 64000, 96000, 128000};
    private ItemStackHandler containerInventory;
    private boolean isExportHatch;

    public MetaTileEntityFluidHatch(String metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier);
        this.containerInventory = new ItemStackHandler(2);
        this.isExportHatch = isExportHatch;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityFluidHatch(metaTileEntityId, getTier(), isExportHatch);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ContainerInventory", containerInventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.containerInventory.deserializeNBT(data.getCompoundTag("ContainerInventory"));
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, containerInventory);
    }

    @Override
    public void update() {
        super.update();
        if(!getWorld().isRemote && getTimer() % 5 == 0) {
            if(isExportHatch) {
                fillContainerFromInternalTank(containerInventory, containerInventory, 0, 1);
                pushFluidsIntoNearbyHandlers(getFrontFacing());
            } else {
                fillInternalTankFromFluidContainer(containerInventory, containerInventory, 0, 1);
                pullFluidsFromNearbyHandlers(getFrontFacing());
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, pipeline);
        (isExportHatch ? Textures.PIPE_OUT_OVERLAY : Textures.PIPE_IN_OVERLAY).renderSided(getFrontFacing(), renderState, pipeline);
    }

    private int getInventorySize() {
        return INVENTORY_SIZES[MathHelper.clamp(getTier(), 0, INVENTORY_SIZES.length - 1)];
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return isExportHatch ? new FluidTankList() : new FluidTankList(new FluidTank(getInventorySize()));
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return isExportHatch ? new FluidTankList(new FluidTank(getInventorySize())) : new FluidTankList();
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return isExportHatch ? MultiblockAbility.EXPORT_FLUIDS : MultiblockAbility.IMPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(List<IFluidTank> abilityList) {
        abilityList.addAll(isExportHatch ? this.exportFluids.getFluidTanks() : this.importFluids.getFluidTanks());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createTankUI((isExportHatch ? exportFluids : importFluids).getTankAt(0), containerInventory, getMetaName(), entityPlayer)
            .build(getHolder(), entityPlayer);
    }

    public static ModularUI.Builder createTankUI(IFluidTank fluidTank, IItemHandlerModifiable containerInventory, String title, EntityPlayer entityPlayer) {
        Builder builder = ModularUI.defaultBuilder();
        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY);
        TankWidget tankWidget = new TankWidget(fluidTank, 67, 50, 18, 18)
            .setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 0xFFFFFF);
        builder.dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 0xFFFFFF);
        return builder.label(6, 6, title)
            .widget(new FluidContainerSlotWidget(containerInventory, 0, 90, 17)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
            .widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON))
            .widget(new SlotWidget(containerInventory, 1, 90, 54, true, false)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
            .bindPlayerInventory(entityPlayer.inventory);
    }

}