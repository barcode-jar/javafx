package sample;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** すべての種類で、画像と SVG が作れることを確かめる。 */
class BarcodeRendererTest {

    @ParameterizedTest
    @EnumSource(BarcodeKind.class)
    void rendersImageAndSvg(BarcodeKind kind) throws Exception {
        BarcodeRenderer.Options o = new BarcodeRenderer.Options();
        o.kind = kind;
        o.data = kind.sampleData;
        o.width = kind.defaultWidth;
        o.height = kind.defaultHeight;
        o.postalPoint = kind.defaultWidth;

        BufferedImage img = BarcodeRenderer.renderImage(o);
        assertTrue(countDark(img) > 200, kind + " の画像に黒い部分がない");

        String svg = BarcodeRenderer.renderSvg(o);
        assertTrue(svg.contains("<svg"), kind + " の SVG が作れない");
    }

    private static int countDark(BufferedImage img) {
        int n = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff, g = (rgb >> 8) & 0xff, b = rgb & 0xff;
                if (r < 80 && g < 80 && b < 80) {
                    n++;
                }
            }
        }
        return n;
    }
}
