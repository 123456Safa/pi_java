package controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import models.CommandeConfirmation;
import models.LigneCommandes;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FactureController {

    @FXML private Label commandeIdLabel;
    @FXML private Label dateLabel;
    @FXML private Label clientNomLabel;
    @FXML private Label modePaiementLabel;
    @FXML private Label statutLabel;
    @FXML private Label sousTotalLabel;
    @FXML private Label tvaLabel;
    @FXML private Label totalLabel;
    @FXML private Label bannerSubtitle;
    @FXML private ImageView qrCodeImage;
    @FXML private VBox lignesContainer;

    private CommandeConfirmation confirmation;

    public void setConfirmation(CommandeConfirmation confirmation) {
        if (confirmation == null) return;
        this.confirmation = confirmation;

        // Header fields
        commandeIdLabel.setText(String.valueOf(confirmation.getCommandeId()));
        clientNomLabel.setText(confirmation.getClient().getNom().toUpperCase());
        modePaiementLabel.setText(confirmation.getModePaiement());

        // Update statut badge based on payment method
        if (statutLabel != null) {
            statutLabel.getStyleClass().removeAll("badge-confirmed", "badge-pending");
            if ("Carte bancaire".equalsIgnoreCase(confirmation.getModePaiement())) {
                statutLabel.setText("✓  PAYÉE");
                statutLabel.getStyleClass().add("badge-confirmed");
            } else {
                statutLabel.setText("⏳  EN ATTENTE");
                statutLabel.getStyleClass().add("badge-pending");
            }
        }


        // Trim date to YYYY-MM-DD
        String dateRaw = confirmation.getDateCommande();
        dateLabel.setText(dateRaw != null && dateRaw.length() >= 10 ? dateRaw.substring(0, 10) : dateRaw);

        // Banner subtitle
        if (bannerSubtitle != null) {
            bannerSubtitle.setText("Commande #" + confirmation.getCommandeId()
                    + " enregistrée le " + (dateRaw != null && dateRaw.length() >= 10 ? dateRaw.substring(0, 10) : dateRaw)
                    + ". Vous recevrez votre livraison sous peu.");
        }

        // Totals
        sousTotalLabel.setText(String.format("%.2f DT", confirmation.getSousTotal()));
        tvaLabel.setText(String.format("%.2f DT", confirmation.getTva()));
        totalLabel.setText(String.format("%.2f DT", confirmation.getTotalTtc()));

        // Products
        populateLignes(confirmation.getLignes());

        // QR Code (async image load)
        generateQRCode(confirmation);
    }

    // ── Build product rows ───────────────────────────────────────────────────
    private void populateLignes(List<LigneCommandes> lignes) {
        lignesContainer.getChildren().clear();
        boolean even = true;
        for (LigneCommandes l : lignes) {
            HBox row = new HBox(0);
            row.getStyleClass().add(even ? "item-row-even" : "item-row-odd");
            row.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(l.getNom());
            nameLabel.getStyleClass().add("cell-product");
            nameLabel.setPrefWidth(300);
            nameLabel.setWrapText(true);

            Label priceLabel = new Label(String.format("%.2f DT", l.getPrix()));
            priceLabel.getStyleClass().add("cell-price");
            priceLabel.setPrefWidth(140);

            Label qtyLabel = new Label(String.valueOf(l.getQuantite()));
            qtyLabel.getStyleClass().add("cell-qty");
            qtyLabel.setPrefWidth(80);

            Label subLabel = new Label(String.format("%.2f DT", l.getSousTotal()));
            subLabel.getStyleClass().add("cell-subtotal");
            HBox.setHgrow(subLabel, Priority.ALWAYS);

            row.getChildren().addAll(nameLabel, priceLabel, qtyLabel, subLabel);
            lignesContainer.getChildren().add(row);
            even = !even;
        }
    }

    // ── QR Code ─────────────────────────────────────────────────────────────
    private void generateQRCode(CommandeConfirmation confirmation) {
        try {
            String data = "PHARMAX | Commande #" + confirmation.getCommandeId()
                    + " | Client: " + confirmation.getClient().getNom()
                    + " | Total: " + String.format("%.2f DT", confirmation.getTotalTtc())
                    + " | Paiement: " + confirmation.getModePaiement();

            String encoded = URLEncoder.encode(data, StandardCharsets.UTF_8.toString());
            String url = "https://api.qrserver.com/v1/create-qr-code/?size=200x200"
                    + "&color=0f172a&bgcolor=ffffff&qzone=1&data=" + encoded;

            Image qrImage = new Image(url, true);
            qrCodeImage.setImage(qrImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Actions ──────────────────────────────────────────────────────────────
    @FXML
    private void handleClose() {
        ((javafx.stage.Stage) commandeIdLabel.getScene().getWindow()).close();
    }

    @FXML
    private void handleDownloadPdf() {
        if (confirmation == null) return;

        String fileName = "Facture_PHARMAX_" + confirmation.getCommandeId() + ".pdf";
        Document document = new Document(PageSize.A4, 42, 42, 54, 42);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // ── Fonts ──
            Font fBrand     = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD,   new BaseColor(15, 23, 42));
            Font fTagline   = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, new BaseColor(100,116,139));
            Font fWord      = new Font(Font.FontFamily.HELVETICA, 26, Font.BOLD,   new BaseColor(109, 93, 252));
            Font fLabel     = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   new BaseColor(148,163,184));
            Font fValue     = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   new BaseColor(30, 41, 59));
            Font fClientBig = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD,   new BaseColor(15, 23, 42));
            Font fColHead   = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   BaseColor.WHITE);
            Font fCell      = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(71, 85,105));
            Font fCellBold  = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   new BaseColor(15, 23, 42));
            Font fTotal     = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, new BaseColor(100,116,139));
            Font fTotalVal  = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   new BaseColor(51, 65, 85));
            Font fGrandLbl  = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD,   new BaseColor(15, 23, 42));
            Font fGrandVal  = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD,   new BaseColor(22,163, 74));
            Font fFooter    = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, new BaseColor(148,163,184));

            LineSeparator thinLine = new LineSeparator(0.5f, 100, new BaseColor(226, 232, 240), Element.ALIGN_LEFT, -2);

            // ── Header table (brand | FACTURE) ──
            PdfPTable header = new PdfPTable(new float[]{60, 40});
            header.setWidthPercentage(100);
            header.setSpacingAfter(0);

            PdfPCell brandCell = new PdfPCell();
            brandCell.setBackgroundColor(new BaseColor(15, 23, 42));
            brandCell.setBorder(Rectangle.NO_BORDER);
            brandCell.setPadding(20);
            brandCell.addElement(new Paragraph("PHARMAX", fBrand));
            brandCell.addElement(new Paragraph("E-Pharmacie · Santé & Bien-être", fTagline));
            header.addCell(brandCell);

            PdfPCell invoiceCell = new PdfPCell();
            invoiceCell.setBackgroundColor(new BaseColor(15, 23, 42));
            invoiceCell.setBorder(Rectangle.NO_BORDER);
            invoiceCell.setPadding(20);
            invoiceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph invoiceWord = new Paragraph("FACTURE", fWord);
            invoiceWord.setAlignment(Element.ALIGN_RIGHT);
            invoiceCell.addElement(invoiceWord);
            Paragraph idPara = new Paragraph("N°  " + confirmation.getCommandeId(),
                    new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.WHITE));
            idPara.setAlignment(Element.ALIGN_RIGHT);
            invoiceCell.addElement(idPara);
            header.addCell(invoiceCell);
            document.add(header);

            // ── Meta row ──
            PdfPTable meta = new PdfPTable(new float[]{25, 25, 25, 25});
            meta.setWidthPercentage(100);
            meta.setSpacingAfter(0);
            BaseColor metaBg = new BaseColor(248, 250, 252);

            addMetaCell(meta, "DATE D'ÉMISSION",
                    confirmation.getDateCommande().length() >= 10
                            ? confirmation.getDateCommande().substring(0, 10) : confirmation.getDateCommande(),
                    fLabel, fValue, metaBg);
            String statutTxt;
            BaseColor statutColor;
            if ("Carte bancaire".equalsIgnoreCase(confirmation.getModePaiement())) {
                statutTxt = "✓  PAYÉE";
                statutColor = new BaseColor(21,128,61);
            } else {
                statutTxt = "⏳  EN ATTENTE";
                statutColor = new BaseColor(180,83,9);
            }
            addMetaCell(meta, "STATUT", statutTxt,
                    fLabel, new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, statutColor), metaBg);
            addMetaCell(meta, "MODE DE PAIEMENT", confirmation.getModePaiement(), fLabel, fValue, metaBg);
            addMetaCell(meta, "FACTURÉ À", confirmation.getClient().getNom().toUpperCase(), fLabel, fClientBig, metaBg);
            document.add(meta);

            document.add(new Chunk(thinLine));
            document.add(new Paragraph("\n"));

            // ── Items table ──
            PdfPTable table = new PdfPTable(new float[]{40, 20, 10, 20});
            table.setWidthPercentage(100);
            table.setSpacingAfter(0);
            BaseColor darkBg = new BaseColor(15, 23, 42);
            String[] cols = {"PRODUIT", "PRIX UNIT.", "QTÉ", "SOUS-TOTAL"};
            int[]    alns = {Element.ALIGN_LEFT, Element.ALIGN_CENTER, Element.ALIGN_CENTER, Element.ALIGN_RIGHT};
            for (int i = 0; i < cols.length; i++) {
                PdfPCell c = new PdfPCell(new Phrase(cols[i], fColHead));
                c.setBackgroundColor(darkBg);
                c.setHorizontalAlignment(alns[i]);
                c.setPadding(9);
                c.setBorder(Rectangle.NO_BORDER);
                table.addCell(c);
            }

            boolean altRow = false;
            for (LigneCommandes l : confirmation.getLignes()) {
                BaseColor rowBg = altRow ? new BaseColor(250, 251, 255) : BaseColor.WHITE;
                BaseColor sep   = new BaseColor(241, 245, 249);

                addItemCell(table, l.getNom(),                             fCellBold, Element.ALIGN_LEFT,   rowBg, sep);
                addItemCell(table, String.format("%.2f DT", l.getPrix()),  fCell,     Element.ALIGN_CENTER, rowBg, sep);
                addItemCell(table, String.valueOf(l.getQuantite()),        fCell,     Element.ALIGN_CENTER, rowBg, sep);
                addItemCell(table, String.format("%.2f DT", l.getSousTotal()), fCellBold, Element.ALIGN_RIGHT, rowBg, sep);
                altRow = !altRow;
            }
            document.add(table);
            document.add(new Chunk(thinLine));

            // ── Totals ──
            PdfPTable totals = new PdfPTable(new float[]{60, 40});
            totals.setWidthPercentage(100);
            totals.setSpacingBefore(2);
            totals.setSpacingAfter(4);
            addTotalRow(totals, "Sous-total (HT)", String.format("%.2f DT", confirmation.getSousTotal()), fTotal, fTotalVal, BaseColor.WHITE, false);
            addTotalRow(totals, "TVA (19%)",       String.format("%.2f DT", confirmation.getTva()),       fTotal, fTotalVal, BaseColor.WHITE, false);
            addTotalRow(totals, "TOTAL TTC",       String.format("%.2f DT", confirmation.getTotalTtc()),  fGrandLbl, fGrandVal, new BaseColor(240,253,244), true);
            document.add(totals);

            document.add(new Chunk(thinLine));

            // ── Footer ──
            Paragraph footer = new Paragraph(
                    "Merci pour votre confiance · pharmax.tn · support@pharmax.tn", fFooter);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(12);
            document.add(footer);

            document.close();

            javafx.scene.control.Alert ok = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            ok.setTitle("Facture générée");
            ok.setHeaderText(null);
            ok.setContentText("Le fichier '" + fileName + "' a été enregistré.");
            ok.show();

            if (java.awt.Desktop.isDesktopSupported())
                java.awt.Desktop.getDesktop().open(new java.io.File(fileName));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── PDF helpers ──────────────────────────────────────────────────────────
    private void addMetaCell(PdfPTable t, String label, String value, Font lf, Font vf, BaseColor bg) {
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(bg);
        c.setBorder(Rectangle.RIGHT);
        c.setBorderColor(new BaseColor(226,232,240));
        c.setPadding(14);
        c.addElement(new Paragraph(label, lf));
        c.addElement(new Paragraph(value, vf));
        t.addCell(c);
    }

    private void addItemCell(PdfPTable t, String text, Font f, int align, BaseColor bg, BaseColor borderColor) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(bg);
        c.setHorizontalAlignment(align);
        c.setPadding(9);
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(borderColor);
        t.addCell(c);
    }

    private void addTotalRow(PdfPTable t, String label, String value, Font lf, Font vf, BaseColor bg, boolean last) {
        PdfPCell lc = new PdfPCell(new Phrase(label, lf));
        lc.setBorder(last ? Rectangle.TOP : Rectangle.NO_BORDER);
        lc.setBorderColor(new BaseColor(226,232,240));
        lc.setBackgroundColor(bg);
        lc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        lc.setPadding(last ? 12 : 7);
        t.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, vf));
        vc.setBorder(last ? Rectangle.TOP : Rectangle.NO_BORDER);
        vc.setBorderColor(new BaseColor(226,232,240));
        vc.setBackgroundColor(bg);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setPadding(last ? 12 : 7);
        t.addCell(vc);
    }
}
