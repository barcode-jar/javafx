package sample;

/** サンプルで選べるバーコードの種類と、最初に入れておくデータ・大きさ。 */
public enum BarcodeKind {
    CODE128("CODE128", "Pao-2026-ABC", Shape.LINEAR, 420, 110),
    CODE39("CODE39", "PAO-123456", Shape.LINEAR, 420, 110),
    CODE93("CODE93", "PAO123456", Shape.LINEAR, 360, 110),
    GS1_128("GS1-128", "{FNC1}0114912345678904{FNC1}17260101{FNC1}10ABC123", Shape.LINEAR, 520, 110),
    NW7("NW7（Codabar）", "A1234567890A", Shape.LINEAR, 380, 110),
    ITF("ITF", "12345678901231", Shape.LINEAR, 380, 110),
    JAN13("JAN-13（EAN-13）", "490123456789", Shape.LINEAR, 300, 110),
    JAN8("JAN-8（EAN-8）", "4901234", Shape.LINEAR, 220, 110),
    UPC_A("UPC-A", "01234567890", Shape.LINEAR, 300, 110),
    UPC_E("UPC-E", "123456", Shape.LINEAR, 180, 110),
    MATRIX2OF5("Matrix 2of5", "1234567890", Shape.LINEAR, 380, 110),
    NEC2OF5("NEC 2of5", "1234567890", Shape.LINEAR, 380, 110),
    DATABAR14("GS1 DataBar 標準型", "0491234567890", Shape.LINEAR, 300, 110),
    DATABAR_LIMITED("GS1 DataBar 限定型", "1234567890123", Shape.LINEAR, 260, 110),
    DATABAR_EXPANDED("GS1 DataBar 拡張型", "0100012345678905{AI}10ABC123", Shape.LINEAR, 480, 110),
    CONVENIENCE("コンビニ収納用（GS1-128）", "{FNC1}91912345000000000000004520875004013100295004", Shape.LINEAR, 640, 110),
    QR("QRコード", "https://www.pao.ac/barcode.jar/", Shape.SQUARE, 240, 240),
    DATAMATRIX("DataMatrix", "Barcode.jar DataMatrix 2026", Shape.SQUARE, 240, 240),
    PDF417("PDF417", "Barcode.jar PDF417 サンプル", Shape.WIDE, 480, 160),
    YUBIN("郵便カスタマバーコード", "27500263-29-2-401", Shape.POSTAL, 12, 0);

    /** 描く領域の形。 */
    public enum Shape { LINEAR, SQUARE, WIDE, POSTAL }

    public final String label;
    public final String sampleData;
    public final Shape shape;
    /** 既定の幅（px）。QR・DataMatrix は一辺、郵便は大きさ（pt）。 */
    public final int defaultWidth;
    /** 既定の高さ（px）。 */
    public final int defaultHeight;

    BarcodeKind(String label, String sampleData, Shape shape, int defaultWidth, int defaultHeight) {
        this.label = label;
        this.sampleData = sampleData;
        this.shape = shape;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
    }

    /** 線の描き方（draw / drawDirect / drawDelicate）を選べる 1 次元のバーコードか。 */
    public boolean isLinear() {
        return shape == Shape.LINEAR;
    }

    @Override
    public String toString() {
        return label;
    }
}
