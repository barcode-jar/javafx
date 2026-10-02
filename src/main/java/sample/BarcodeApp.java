package sample;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import pao.barcode.CodeSet128;
import pao.barcode.ProductInfo;

/** Barcode.jar の JavaFX サンプル。種類とデータを選ぶと、その場でバーコードが描かれます。 */
public class BarcodeApp extends Application {

    private final BarcodeRenderer.Options options = new BarcodeRenderer.Options();

    private final ComboBox<BarcodeKind> kindBox = new ComboBox<>();
    private final TextField dataField = new TextField();
    private final Spinner<Integer> widthSpinner = new Spinner<>(1, 2000, 480, 10);
    private final Spinner<Integer> heightSpinner = new Spinner<>(10, 1000, 120, 10);
    private final ComboBox<BarcodeRenderer.LineMode> lineModeBox = new ComboBox<>();
    private final CheckBox showTextCheck = new CheckBox("バーコードの下に文字を書く");
    private final CheckBox evenSpacingCheck = new CheckBox("文字を均等に並べる");
    private final ComboBox<CodeSet128> codeSetBox = new ComboBox<>();
    private final ComboBox<String> qrEccBox = new ComboBox<>();
    private final CheckBox stackedCheck = new CheckBox("多段（Stacked）にする");
    private final Label widthLabel = new Label("幅（px）");
    private final Label heightLabel = new Label("高さ（px）");

    private final ImageView preview = new ImageView();
    private final Label message = new Label();
    private BufferedImage current;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        kindBox.getItems().addAll(BarcodeKind.values());
        lineModeBox.getItems().addAll(BarcodeRenderer.LineMode.values());
        codeSetBox.getItems().addAll(CodeSet128.values());
        qrEccBox.getItems().addAll("L", "M", "Q", "H");
        kindBox.setValue(BarcodeKind.CODE128);
        lineModeBox.setValue(BarcodeRenderer.LineMode.FIT);
        codeSetBox.setValue(CodeSet128.AUTO);
        qrEccBox.setValue("M");
        showTextCheck.setSelected(true);
        evenSpacingCheck.setSelected(true);
        widthSpinner.setEditable(true);
        heightSpinner.setEditable(true);
        dataField.setPrefColumnCount(28);

        kindBox.setOnAction(e -> onKindChanged());
        lineModeBox.setOnAction(e -> onLineModeChanged());
        dataField.textProperty().addListener((o, a, b) -> refresh());
        widthSpinner.valueProperty().addListener((o, a, b) -> refresh());
        heightSpinner.valueProperty().addListener((o, a, b) -> refresh());
        showTextCheck.setOnAction(e -> refresh());
        evenSpacingCheck.setOnAction(e -> refresh());
        stackedCheck.setOnAction(e -> refresh());
        codeSetBox.setOnAction(e -> refresh());
        qrEccBox.setOnAction(e -> refresh());

        Button savePng = new Button("PNG で保存");
        Button saveSvg = new Button("SVG で保存");
        Button print = new Button("印刷");
        savePng.setOnAction(e -> savePng(stage));
        saveSvg.setOnAction(e -> saveSvg(stage));
        print.setOnAction(e -> print(stage));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.getColumnConstraints().add(new ColumnConstraints(120));
        int r = 0;
        form.addRow(r++, new Label("種類"), kindBox);
        form.addRow(r++, new Label("データ"), dataField);
        form.addRow(r++, widthLabel, widthSpinner);
        form.addRow(r++, heightLabel, heightSpinner);
        form.addRow(r++, new Label("線の描き方"), lineModeBox);
        form.addRow(r++, new Label("コードセット"), codeSetBox);
        form.addRow(r++, new Label("誤り訂正（QR）"), qrEccBox);
        form.add(showTextCheck, 1, r++);
        form.add(evenSpacingCheck, 1, r++);
        form.add(stackedCheck, 1, r++);

        HBox buttons = new HBox(10, savePng, saveSvg, print);
        Label license = new Label(ProductInfo.isLicensed()
                ? "ライセンス：" + ProductInfo.getLicenseEmailAddr()
                : "体験版です。バーコードに赤い「SAMPLE」が入ります。");
        license.setWrapText(true);
        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #c62828;");

        VBox left = new VBox(14, form, buttons, license, message);
        left.setPadding(new Insets(16));
        left.setPrefWidth(460);

        StackPane canvas = new StackPane(preview);
        canvas.setAlignment(Pos.CENTER);
        canvas.setPadding(new Insets(16));
        canvas.setStyle("-fx-background-color: #eef1f5;");
        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        BorderPane root = new BorderPane(scroll);
        root.setLeft(left);
        stage.setTitle("Barcode.jar サンプル（JavaFX）  " + ProductInfo.getVersion());
        stage.setScene(new Scene(root, 1180, 640));
        stage.show();
        onKindChanged();
    }

    /** 種類を変えたら、データと大きさをその種類の例にする。 */
    private void onKindChanged() {
        BarcodeKind kind = kindBox.getValue();
        dataField.setText(kind.sampleData);
        lineModeBox.setValue(BarcodeRenderer.LineMode.FIT);
        widthSpinner.getValueFactory().setValue(kind.defaultWidth);
        heightSpinner.getValueFactory().setValue(Math.max(kind.defaultHeight, 10));
        boolean linear = kind.isLinear();
        lineModeBox.setDisable(!linear);
        showTextCheck.setDisable(!linear);
        evenSpacingCheck.setDisable(!linear);
        codeSetBox.setDisable(kind != BarcodeKind.CODE128 && kind != BarcodeKind.GS1_128);
        qrEccBox.setDisable(kind != BarcodeKind.QR);
        stackedCheck.setDisable(kind != BarcodeKind.DATABAR14 && kind != BarcodeKind.DATABAR_EXPANDED);
        heightSpinner.setDisable(kind.shape == BarcodeKind.Shape.SQUARE || kind.shape == BarcodeKind.Shape.POSTAL);
        widthLabel.setText(kind.shape == BarcodeKind.Shape.POSTAL ? "大きさ（pt）"
                : kind.shape == BarcodeKind.Shape.SQUARE ? "一辺（px）" : "幅（px）");
        refresh();
    }

    private void onLineModeChanged() {
        boolean delicate = lineModeBox.getValue() == BarcodeRenderer.LineMode.DELICATE;
        widthLabel.setText(delicate ? "細いバーの幅（px）" : "幅（px）");
        widthSpinner.getValueFactory().setValue(delicate ? 2 : kindBox.getValue().defaultWidth);
        refresh();
    }

    /** 画面の値を Options に移して、プレビューを描き直す。 */
    private void refresh() {
        if (kindBox.getValue() == null) {
            return;
        }
        options.kind = kindBox.getValue();
        options.data = dataField.getText();
        options.lineMode = lineModeBox.getValue();
        options.width = widthSpinner.getValue();
        options.height = heightSpinner.getValue();
        options.postalPoint = widthSpinner.getValue();
        options.showText = showTextCheck.isSelected();
        options.evenSpacing = evenSpacingCheck.isSelected();
        options.codeSet = codeSetBox.getValue();
        options.qrErrorCorrect = qrEccBox.getValue();
        options.stackedDatabar = stackedCheck.isSelected();
        try {
            current = BarcodeRenderer.renderImage(options);
            preview.setImage(toFxImage(current));
            message.setText("");
        } catch (Exception | Error ex) {
            current = null;
            preview.setImage(null);
            message.setText("描けませんでした：" + ex.getMessage());
        }
    }

    private void savePng(Stage stage) {
        File file = choose(stage, "barcode.png", "PNG 画像", "*.png");
        if (file == null || current == null) {
            return;
        }
        try {
            ImageIO.write(current, "png", file);
            message.setText("");
        } catch (Exception ex) {
            message.setText("保存できませんでした：" + ex.getMessage());
        }
    }

    private void saveSvg(Stage stage) {
        File file = choose(stage, "barcode.svg", "SVG 画像", "*.svg");
        if (file == null) {
            return;
        }
        try {
            Files.writeString(file.toPath(), BarcodeRenderer.renderSvg(options), StandardCharsets.UTF_8);
            message.setText("");
        } catch (Exception ex) {
            message.setText("保存できませんでした：" + ex.getMessage());
        }
    }

    /** プレビューの画像を、用紙の左上に原寸（96dpi 相当）で印刷する。 */
    private void print(Stage stage) {
        if (current == null) {
            return;
        }
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null || !job.showPrintDialog(stage)) {
            return;
        }
        PageLayout page = job.getJobSettings().getPageLayout();
        ImageView view = new ImageView(toFxImage(current));
        view.setPreserveRatio(true);
        double scale = 72.0 / 96.0;
        view.setFitWidth(Math.min(current.getWidth() * scale, page.getPrintableWidth()));
        if (job.printPage(view)) {
            job.endJob();
        }
    }

    private static File choose(Stage stage, String name, String description, String pattern) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(name);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, pattern));
        return chooser.showSaveDialog(stage);
    }

    private static Image toFxImage(BufferedImage img) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "png", out);
            return new Image(new ByteArrayInputStream(out.toByteArray()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
