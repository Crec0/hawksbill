package club.mindtech.mindbot.helpers.image

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Toolkit
import java.awt.image.BufferedImage

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
    color: Int = 0xFFFFFF,
    fill: Boolean = false
) {
    this.color = Color(color)
    if (fill) {
        this.fillRect(x, y, width, height)
    } else {
        this.drawRect(x, y, width, height)
    }
}

fun Graphics2D.text(x: Int, y: Int, text: String, font: String = "oxygen", style: Int = Font.PLAIN, size: Int = 18, color: Int = 0xFFFFFF) {
    this.font = Font(font, style, size)
    this.color = Color(color)
    this.drawString(text, x, y)
}

fun Graphics2D.ellipse(x: Int, y: Int, width: Int, height: Int) {
    this.drawOval(x, y, width, height)
}
