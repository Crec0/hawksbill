package dev.crec.hawksbill.helpers

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Toolkit
import java.awt.image.BufferedImage

enum class Colors(val hex: Int) {
    BLUE_GRAY_600(0x475569),
    LIGHT_BLUE_400(0x38BDF8),
    EMERALD_400(0x34D399),
    AMBER_400(0xFBBF24),
    VIOLET_200(0xDDD6FE),
    GRAY_100(0xF4F4F5),
    WHITE(0xFFFFFF)
}

fun Int.ptToPx(): Int {
    return this * 72 / 96
}

fun image(width: Int, height: Int, components: Graphics2D.() -> Unit): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2d = img.createGraphics()
    // https://stackoverflow.com/questions/31536952/how-to-fix-text-quality-in-java-graphics
    Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")?.let {
        g2d.setRenderingHints(it as Map<*, *>)
    }
    g2d.apply(components)
    g2d.dispose()
    return img
}

fun Graphics2D.rect(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    color: Colors = Colors.WHITE,
    fill: Boolean = false
) {
    this.color = Color(color.hex)
    if (fill) {
        this.fillRect(x, y, width, height)
    } else {
        this.drawRect(x, y, width, height)
    }
}

fun Graphics2D.text(
    x: Int,
    y: Int,
    text: String,
    fontName: String = "Arial",
    style: Int = Font.PLAIN,
    size: Int = 18,
    font: Font = Font(fontName, style, size),
    color: Colors = Colors.WHITE
) {
    this.font = font
    this.color = Color(color.hex)
    this.drawString(text, x, y)
}

fun Graphics2D.ellipse(x: Int, y: Int, width: Int, height: Int) {
    this.drawOval(x, y, width, height)
}
