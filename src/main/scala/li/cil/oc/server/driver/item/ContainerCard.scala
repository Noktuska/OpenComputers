package li.cil.oc.server.driver.item

import li.cil.oc.api.driver.{Container, EnvironmentHost}
import li.cil.oc.common.{Slot, item}
import li.cil.oc.{Items, api}
import net.minecraft.item.ItemStack

object ContainerCard extends Item with Container {
  override def worksWith(stack: ItemStack, host: EnvironmentHost) = isOneOf(stack, api.Items.get("cardContainer1"), api.Items.get("cardContainer2"), api.Items.get("cardContainer3"))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) = null

  override def slot(stack: ItemStack) = Slot.Container

  override def providedSlot(stack: ItemStack) = Slot.Card

  override def providedTier(stack: ItemStack) = tier(stack)

  override def tier(stack: ItemStack) =
    Items.multi.subItem(stack) match {
      case Some(container: item.UpgradeContainerCard) => container.tier
      case _ => 0
    }
}
