package li.cil.oc.client.gui

import java.util

import com.google.common.base.Charsets
import li.cil.oc.Settings
import li.cil.oc.client.Textures
import li.cil.oc.util.PseudoMarkdown
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse

import scala.collection.convert.WrapAsJava._
import scala.io.Source

class Manual extends GuiScreen {
  var guiLeft = 0
  var guiTop = 0
  var xSize = 0
  var ySize = 0
  var offset = 0
  var document = Iterable.empty[PseudoMarkdown.Segment]
  var documentHeight = 0
  final val documentMaxWidth = 230
  final val documentMaxHeight = 176
  final val scrollPosX = 244
  final val scrollPosY = 6
  final val scrollHeight = 180

  def add[T](list: util.List[T], value: Any) = list.add(value.asInstanceOf[T])

  protected var scrollButton: ImageButton = _

  def loadPage(location: ResourceLocation): Iterator[String] = {
    val resource = Minecraft.getMinecraft.getResourceManager.getResource(location)
    val is = resource.getInputStream
    Source.fromInputStream(is)(Charsets.UTF_8).getLines()
  }

  override def doesGuiPauseGame = false

  override def initGui(): Unit = {
    super.initGui()

    val mc = Minecraft.getMinecraft
    val screenSize = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight)
    val guiSize = new ScaledResolution(mc, 256, 192)
    val (midX, midY) = (screenSize.getScaledWidth / 2, screenSize.getScaledHeight / 2)
    guiLeft = midX - guiSize.getScaledWidth / 2
    guiTop = midY - guiSize.getScaledHeight / 2
    xSize = guiSize.getScaledWidth
    ySize = guiSize.getScaledHeight
    offset = 0
    document = PseudoMarkdown.parse(loadPage(new ResourceLocation(Settings.resourceDomain, "doc/index.md")))
    documentHeight = PseudoMarkdown.height(document, documentMaxWidth, fontRendererObj)

    scrollButton = new ImageButton(1, guiLeft + scrollPosX, guiTop + scrollPosY, 6, 13, Textures.guiButtonScroll)
    add(buttonList, scrollButton)
  }

  override def drawScreen(mouseX: Int, mouseY: Int, dt: Float): Unit = {
    mc.renderEngine.bindTexture(Textures.guiManual)
    Gui.func_146110_a(guiLeft, guiTop, 0, 0, xSize, ySize, 256, 192)

    super.drawScreen(mouseX, mouseY, dt)

    PseudoMarkdown.render(document, guiLeft + 8, guiTop + 8, documentMaxWidth, documentMaxHeight, offset, fontRendererObj, mouseX, mouseY) match {
      case Some(segment) => segment.tooltip match {
        case Some(text) if text.nonEmpty => drawHoveringText(seqAsJavaList(text.lines.toSeq), mouseX, mouseY, fontRendererObj)
        case _ =>
      }
      case _ =>
    }
  }

  override def handleMouseInput(): Unit = {
    super.handleMouseInput()
    if (Mouse.hasWheel && Mouse.getEventDWheel != 0) {
      if (math.signum(Mouse.getEventDWheel) < 0) scrollDown()
      else scrollUp()
    }
  }

  private def scrollUp() = scrollTo(offset - PseudoMarkdown.lineHeight(fontRendererObj) * 3)

  private def scrollDown() = scrollTo(offset + PseudoMarkdown.lineHeight(fontRendererObj) * 3)

  private def scrollTo(row: Int): Unit = {
    val maxOffset = documentHeight - documentMaxHeight
    offset = math.max(0, math.min(maxOffset, row))
    val yMin = guiTop + scrollPosY
    if (maxOffset > 0) {
      scrollButton.yPosition = yMin + (scrollHeight - 13) * offset / maxOffset
    }
    else {
      scrollButton.yPosition = yMin
    }
  }
}
