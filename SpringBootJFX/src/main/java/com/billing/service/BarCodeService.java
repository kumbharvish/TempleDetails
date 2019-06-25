package com.billing.service;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.BarcodeEAN;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
 
public class BarCodeService {
 
    public static void createPdf(String dest,String barcode,String productName,int noOfPages) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();
        Font ffont = new Font(Font.FontFamily.UNDEFINED, 10, Font.BOLD);
        PdfContentByte cb = writer.getDirectContent();
        Phrase header = new Phrase(productName, ffont);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                header,
                (document.right() - document.left()) / 2 + document.leftMargin(),
                document.top() + 10, 0);
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        int noOfBaracodes = noOfPages*52;
        for (int i = 0; i < noOfBaracodes; i++) {
            table.addCell(createBarcode(writer, barcode,productName));
        }
        document.add(table);
        document.close();
    }
 
    public static PdfPCell createBarcode(PdfWriter writer, String code,String productName) throws DocumentException, IOException {
        BarcodeEAN barcode = new BarcodeEAN();
        barcode.setCodeType(Barcode.EAN8);
        barcode.setCode(code);
        PdfPCell cell = new PdfPCell(barcode.createImageWithBarcode(writer.getDirectContent(), BaseColor.BLACK, BaseColor.BLACK), true);
        cell.setPadding(5);
        cell.setFixedHeight(59);
        return cell;
    }
}