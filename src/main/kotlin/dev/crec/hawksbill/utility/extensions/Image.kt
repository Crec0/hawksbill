package dev.crec.hawksbill.utility.extensions

import dev.crec.hawksbill.utility.Colors
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

fun Graphics2D.ellipse(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    color: Colors = Colors.EMERALD_400,
    fill: Boolean = true
) {
    this.color = Color(color.hex)
    if (fill) {
        this.fillOval(x, y, width, height)
    } else {
        this.drawOval(x, y, width, height)
    }
}

//fun main() {
//    val initial = ImageIO.read(File("D:\\git\\Hawksbill\\initial.png"))

//    val circleImage = image(128, 128) {
//        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
//        ellipse(0, 0, 128, 128)
//        composite = AlphaComposite.SrcIn
//        drawImage(initial, null, null)
//    }
//
//    val card = image(512, 256) {
//        rect(0, 0, 512, 256, color = Colors.EMERALD_400, fill = true)
//        drawImage(circleImage, 64, 64, 128, 128, null)
//    }

//    val raster = initial.raster
//
//    for (i in 0 until raster.width) {
//        for (j in 0 until raster.height) {
//            val rgb = raster.getPixel(i, j, IntArray(3))
//            val transformed = rgb.map {
//                ((200 - 55) * it) / (255) + 55
//            }.toIntArray()
//            raster.setPixel(i, j, transformed)
//        }
//    }
//
//    ImageIO.write(initial, "png", File("D:\\git\\Hawksbill\\result.png"))
//}
