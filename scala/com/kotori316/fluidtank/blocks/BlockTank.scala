package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tiers, TileTank}
import com.kotori316.fluidtank.{FluidTank, Utils}
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, ITileEntityProvider}
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.stats.StatList
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{BlockPos, RayTraceResult}
import net.minecraft.util.{BlockRenderLayer, EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.fluids.{Fluid, FluidUtil}
import net.minecraftforge.items.CapabilityItemHandler

import scala.collection.JavaConverters._

abstract class BlockTank extends Block(Utils.MATERIAL) with ITileEntityProvider {

    def rank: Int

    final val itemBlock = new ItemBlockTank(this, rank)

    def getTierByMeta(meta: Int): Tiers = tierArray(meta)

    setRegistryName(FluidTank.modID, "blocktank" + rank)
    setUnlocalizedName(FluidTank.modID + ".blocktank" + rank)
    setCreativeTab(Utils.CREATIVE_TABS)
    setHardness(1.0f)
    itemBlock.setRegistryName(FluidTank.modID, "blocktank" + rank)

    final lazy val tierArray = Tiers.list.filter(t => t.rank == rank).toArray

    override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer,
                                  hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
        val stack = playerIn.getHeldItem(hand)
        val tileTank = worldIn.getTileEntity(pos).asInstanceOf[TileTank]
        if (FluidUtil.getFluidHandler(stack) != null && tileTank != null) {
            if (!worldIn.isRemote) {
                val handler = tileTank.tank
                val result = FluidUtil.tryEmptyContainerAndStow(stack, handler, playerIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP),
                    Fluid.BUCKET_VOLUME, playerIn, true)
                if (result.isSuccess) {
                    playerIn.setHeldItem(hand, result.getResult)
                }
                // TODO send packet to client.
            }
            return true
        }
        super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)
    }

    override final def createNewTileEntity(worldIn: World, meta: Int) = new TileTank(getTierByMeta(meta))

    override final def getBlockLayer = BlockRenderLayer.CUTOUT

    override final def isFullCube(state: IBlockState) = false

    override final def isOpaqueCube(state: IBlockState) = false

    override final def hasTileEntity(state: IBlockState) = true

    override final def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos) = Utils.BOUNDING_BOX

    override final def shouldSideBeRendered(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing) = true

    override def canCreatureSpawn(state: IBlockState, world: IBlockAccess, pos: BlockPos, living: EntityLiving.SpawnPlacementType) = false

    override def breakBlock(worldIn: World, pos: BlockPos, state: IBlockState): Unit = {
        super.breakBlock(worldIn, pos, state)
        worldIn.removeTileEntity(pos)
    }

    override def getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack = {
        val stack = super.getPickBlock(state, target, world, pos, player)
        saveTankNBT(world.getTileEntity(pos), stack)
        stack
    }

    private def saveTankNBT(tileEntity: TileEntity, stack: ItemStack) = {
        Option(tileEntity).collect { case tank: TileTank if tank.shouldSaveToNBT => tank.writeToNBT(new NBTTagCompound) }
          .foreach(tag => stack.setTagInfo("BlockEntityTag", tag))
    }

    override def harvestBlock(worldIn: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, te: TileEntity, stack: ItemStack): Unit = {
        player.addStat(StatList.getBlockStats(this))
        player.addExhaustion(0.005F)
        harvesters.set(player)
        val i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack)
        if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
            val blockStack = new ItemStack(this, 1, damageDropped(state))
            saveTankNBT(te, blockStack)
            val list = Seq(blockStack)
            val chance = ForgeEventFactory.fireBlockHarvesting(list.asJava, worldIn, pos, state, i, 1.0f, false, harvesters.get)
            for (drop <- list) {
                if (worldIn.rand.nextFloat <= chance) Block.spawnAsEntity(worldIn, pos, drop)
            }
        }
        harvesters.set(null)
    }
}

object BlockTank {
    val blockTank1 = new BlockTank {
        override def rank = 1

        override def getTierByMeta(meta: Int) = Tiers.WOOD
    }
    val blockTank2 = new BlockTankVariants {
        override def rank = 2
    }
    val blockTank3 = new BlockTankVariants {
        override def rank = 3
    }
    val blockTank4 = new BlockTank {
        override def rank = 4

        override def getTierByMeta(meta: Int) = Tiers.GOLD
    }
    val blockTank5 = new BlockTank {
        override def rank = 5

        override def getTierByMeta(meta: Int) = Tiers.DIAMOND
    }
    val blockTank6 = new BlockTank {
        override def rank = 6

        override def getTierByMeta(meta: Int) = Tiers.EMERALD
    }
}