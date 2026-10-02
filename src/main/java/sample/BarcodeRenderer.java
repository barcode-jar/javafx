package sample;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import pao.barcode.Code128;
import pao.barcode.Code39;
import pao.barcode.Code93;
import pao.barcode.CodeSet128;
import pao.barcode.DataMatrix;
import pao.barcode.EAN128;
import pao.barcode.Gs1Databar14;
import pao.barcode.Gs1DatabarExpanded;
import pao.barcode.Gs1DatabarLimited;
import pao.barcode.IBarCode;
import pao.barcode.ITF;
import pao.barcode.Jan13;
import pao.barcode.Jan8;
import pao.barcode.Matrix2of5;
import pao.barcode.NEC2of5;
import pao.barcode.NW7;
import pao.barcode.Pdf417;
import pao.barcode.QRCode;
import pao.barcode.UpcA;
import pao.barcode.UpcE;
import pao.barcode.YubinCustomer;

/**
 * Barcode.jar でバーコードを描く部分。画面にも印刷にも使う画像（PNG）と、SVG の文字列を作る。
 * 画面（JavaFX）には依存しないので、このクラスだけを業務のプログラムに持っていけます。
 */
public final class BarcodeRenderer {

    /** 線の描き方。1 次元のバーコードで使う。 */
    public enum LineMode {
        FIT("幅いっぱいに描く（draw）"),
        DIRECT("バーを整数ドットにそろえる（drawDirect）"),
        DELICATE("いちばん細いバーの幅を指定（drawDelicate）");

        public final String label;

        LineMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /** 描く内容。 */
    public static final class Options {
        public BarcodeKind kind = BarcodeKind.CODE128;
        public String data = kind.sampleData;
        public float width = 480;          // 幅（px）。drawDelicate のときは、いちばん細いバーの幅（px）
        public float height = 120;         // 高さ（px）。郵便は文字の大きさ（pt）として使わない
        public float postalPoint = 12;     // 郵便カスタマバーコードの大きさ（ポイント）
        public LineMode lineMode = LineMode.FIT;
        public boolean showText = true;    // バーコードの下に文字を書く
        public boolean evenSpacing = true; // 文字を幅いっぱいに均等に並べる
        public int fontSize = 14;
        public CodeSet128 codeSet = CodeSet128.AUTO;
        public String qrErrorCorrect = "M";
        public int qrVersion = 0;          // 0 は自動
        public boolean stackedDatabar = false;
    }

    static final int MARGIN = 24;

    private BarcodeRenderer() {
    }

    /** 白地の画像にバーコードを描く。 */
    public static BufferedImage renderImage(Options o) throws Exception {
        int w = Math.round(drawWidth(o)) + MARGIN * 2;
        int h = Math.round(drawHeight(o)) + MARGIN * 2;
        BufferedImage img = new BufferedImage(Math.max(w, 160), Math.max(h, 120), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.BLACK);
            draw(g, o, MARGIN, MARGIN);
        } finally {
            g.dispose();
        }
        return img;
    }

    /** Graphics2D の (x, y) にバーコードを描く。印刷でもこのまま使えます。 */
    public static void draw(Graphics2D g, Options o, float x, float y) throws Exception {
        String data = o.data;
        switch (o.kind) {
            case QR -> {
                QRCode qr = new QRCode(g);
                qr.setErrorCorrect(o.qrErrorCorrect);
                qr.setVersion(o.qrVersion);
                qr.draw(data, x, y, o.width, o.width);
            }
            case DATAMATRIX -> new DataMatrix(g).draw(data, x, y, o.width, o.width);
            case PDF417 -> new Pdf417(g).draw(data, x, y, o.width, o.height);
            case YUBIN -> new YubinCustomer(g).draw(data, x, y, o.postalPoint);
            case CONVENIENCE -> {
                EAN128 b = new EAN128(g);
                setText(b, o);
                switch (o.lineMode) {
                    case FIT -> b.drawConvenience(data, x, y, o.width, o.height,
                            pao.barcode.GraphicsUnit.PIXEL, 96, false);
                    case DIRECT -> b.drawConvenienceDirect(data, x, y, o.width, o.height,
                            pao.barcode.GraphicsUnit.PIXEL, 96, false);
                    case DELICATE -> b.drawConvenienceDelicate(data, x, y, o.width, o.height);
                }
            }
            default -> {
                IBarCode b = createLinear(o, g);
                setText(b, o);
                switch (o.lineMode) {
                    case FIT -> b.draw(data, x, y, o.width, o.height);
                    case DIRECT -> b.drawDirect(data, x, y, o.width, o.height);
                    case DELICATE -> b.drawDelicate(data, x, y, o.width, o.height);
                }
            }
        }
    }

    /** SVG の文字列を作る（Graphics2D を使わない）。 */
    public static String renderSvg(Options o) throws Exception {
        float x = MARGIN, y = MARGIN;
        float w = drawWidth(o), h = o.height;
        String data = o.data;
        return switch (o.kind) {
            case QR -> {
                QRCode qr = new QRCode();
                qr.setErrorCorrect(o.qrErrorCorrect);
                qr.setVersion(o.qrVersion);
                yield qr.writeSVGToString(data, x, y, o.width, o.width);
            }
            case DATAMATRIX -> new DataMatrix().writeSVGToString(data, x, y, o.width, o.width);
            case PDF417 -> new Pdf417().writeSVGToString(data, x, y, o.width, o.height);
            case YUBIN -> new YubinCustomer().writeSVGToString(data, x, y, o.postalPoint);
            case CONVENIENCE -> {
                EAN128 b = new EAN128();
                setText(b, o);
                yield b.writeSVGConvenienceToString(data, x, y, w, h);
            }
            default -> {
                IBarCode b = createLinear(o, null);
                setText(b, o);
                yield writeLinearSvg(b, data, x, y, w, h);
            }
        };
    }

    /** 実際に描かれる幅（px）。drawDelicate は、データから幅が決まる。 */
    static float drawWidth(Options o) {
        if (o.kind == BarcodeKind.YUBIN) {
            return o.postalPoint * 24;   // 郵便は大きさ（pt）から幅が決まる
        }
        if (o.lineMode == LineMode.DELICATE && o.kind.isLinear()) {
            try {
                IBarCode b = o.kind == BarcodeKind.CONVENIENCE ? null : createLinear(o, null);
                if (b != null) {
                    return b.getDrawDelicateWidth(o.data, o.width);
                }
                return new EAN128().getDrawConvenienceDelicateWidth(o.data, o.width);
            } catch (Exception ex) {
                return 600;   // データが正しくないときは仮の幅（描くときにエラーを表示する）
            }
        }
        return o.width;
    }

    static float drawHeight(Options o) {
        return switch (o.kind) {
            case QR, DATAMATRIX -> o.width;
            case YUBIN -> o.postalPoint * 2;
            default -> o.height + (o.showText && o.kind.isLinear() ? o.fontSize * 2 : 0);
        };
    }

    private static IBarCode createLinear(Options o, Graphics2D g) {
        return switch (o.kind) {
            case CODE128 -> {
                Code128 c = g == null ? new Code128() : new Code128(g);
                c.setCodeABC(o.codeSet);
                yield c;
            }
            case GS1_128 -> {
                EAN128 c = g == null ? new EAN128() : new EAN128(g);
                c.setCodeABC(o.codeSet);
                yield c;
            }
            case CODE39 -> g == null ? new Code39() : new Code39(g);
            case CODE93 -> g == null ? new Code93() : new Code93(g);
            case NW7 -> g == null ? new NW7() : new NW7(g);
            case ITF -> g == null ? new ITF() : new ITF(g);
            case JAN13 -> g == null ? new Jan13() : new Jan13(g);
            case JAN8 -> g == null ? new Jan8() : new Jan8(g);
            case UPC_A -> g == null ? new UpcA() : new UpcA(g);
            case UPC_E -> g == null ? new UpcE() : new UpcE(g);
            case MATRIX2OF5 -> g == null ? new Matrix2of5() : new Matrix2of5(g);
            case NEC2OF5 -> g == null ? new NEC2of5() : new NEC2of5(g);
            case DATABAR14 -> {
                Gs1Databar14 c = g == null ? new Gs1Databar14() : new Gs1Databar14(g);
                c.setSymbolType(o.stackedDatabar ? Gs1Databar14.DatabarType.STACKED_OMNIDIRECTIONAL
                        : Gs1Databar14.DatabarType.OMNIDIRECTIONAL);
                yield c;
            }
            case DATABAR_LIMITED -> g == null ? new Gs1DatabarLimited() : new Gs1DatabarLimited(g);
            case DATABAR_EXPANDED -> {
                Gs1DatabarExpanded c = g == null ? new Gs1DatabarExpanded() : new Gs1DatabarExpanded(g);
                c.setSymbolType(o.stackedDatabar ? Gs1DatabarExpanded.DatabarType.STACKED
                        : Gs1DatabarExpanded.DatabarType.UNSTACKED);
                yield c;
            }
            default -> throw new IllegalArgumentException(o.kind.label);
        };
    }

    private static void setText(IBarCode b, Options o) {
        b.setTextWrite(o.showText);
        b.setTextKintou(o.evenSpacing);
        b.setTextFont(new Font(Font.SANS_SERIF, Font.PLAIN, o.fontSize));
    }

    /** writeSVGToString は種類ごとのクラスにあるので、種類に合わせて呼ぶ。 */
    private static String writeLinearSvg(IBarCode b, String data, float x, float y, float w, float h) throws Exception {
        if (b instanceof Code128 c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof EAN128 c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof Code39 c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof Code93 c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof NW7 c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof ITF c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof Jan13 c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof Jan8 c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof UpcA c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof UpcE c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof Matrix2of5 c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof NEC2of5 c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof Gs1Databar14 c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof Gs1DatabarLimited c) return c.writeSVGToString(data, x, y, w, h);
        if (b instanceof Gs1DatabarExpanded c) return c.writeSVGToString(data, x, y, w, h);
        throw new IllegalArgumentException(b.getClass().getSimpleName());
    }
}
