package net.sorenon.images.content.gui

import com.google.common.base.CharMatcher
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.ChatUtil
import net.minecraft.util.Formatting
import net.sorenon.images.init.ImagesMod
import java.net.IDN
import java.net.URL
import java.util.function.Predicate

class SelectImageScreen(private val currentID: String) : Screen(TranslatableText("selectImage.title")){
    private lateinit var urlInput: TextFieldWidget
    private var doneButton: ButtonWidget? = null
    private val addressTextFilter =
        Predicate { string: String ->
            if (ChatUtil.isEmpty(string)) {
                true
            } else {
                CharMatcher.ascii().matchesAllOf(string)
            }
        }

    override fun init() {
        super.init()
        urlInput = TextFieldWidget(textRenderer, 10, height / 2 - 20, width - 20, 20, LiteralText(""))
        urlInput.setMaxLength(128)
        urlInput.setTextPredicate(addressTextFilter)
        urlInput.setChangedListener {
            if (ImagesMod.sanitizeURL(it) != null) {
                urlInput.setEditableColor(14737632)
                doneButton?.active = true
            }
            else {
                urlInput.setEditableColor(16733525)
                doneButton?.active = it.isEmpty()
            }
        }
        urlInput.text = currentID
        urlInput.active = true
        this.children.add(urlInput)
        this.setInitialFocus(urlInput)

        doneButton = addButton(
            ButtonWidget(
                width / 2 - 100,
                height / 2 + 40,
                200,
                20,
                ScreenTexts.DONE
            ) {
                val buf = PacketByteBuf(Unpooled.buffer())
                buf.writeString(urlInput.text)
                ClientSidePacketRegistry.INSTANCE.sendToServer(ImagesMod.C2S_SET_TEXTURE, buf)
                onClose()
            }
        )
        addButton(
            ButtonWidget(
                width / 2 - 100,
                height / 2 + 40 + 24,
                200,
                20,
                ScreenTexts.CANCEL
            ) { onClose() }
        )
        addButton(
            ButtonWidget(
                width / 2 - 100,
                height / 2 + 40 + 24 + 24,
                50,
                20,
                TranslatableText("images.clear")
            ) { urlInput.text = "" }
        )
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == 256) {
            client!!.player!!.closeHandledScreen()
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun tick() {
        super.tick()
        urlInput.tick()
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawCenteredText(matrices, textRenderer, title, width / 2, height / 2 - 70, 16777215)
        urlInput.render(matrices, mouseX, mouseY, delta)
    }
}