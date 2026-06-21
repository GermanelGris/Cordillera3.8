package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.ArchivoReporte;
import com.cordillera.MS_data.dto.EstadisticasStock;
import com.cordillera.MS_data.entity.Inventario;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteGeneradorService {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Genera el reporte en el formato indicado (PDF o EXCEL). */
    public ArchivoReporte generar(String formato, String periodo,
                                  EstadisticasStock stats, List<Inventario> inventario) {
        boolean excel = "EXCEL".equalsIgnoreCase(formato);
        return excel
                ? generarExcel(periodo, stats, inventario)
                : generarPdf(periodo, stats, inventario);
    }

    // ─── PDF (OpenPDF) ───────────────────────────────────────────────────────

    private ArchivoReporte generarPdf(String periodo, EstadisticasStock stats, List<Inventario> inventario) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font tituloFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font subFont = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.DARK_GRAY);
            Font seccionFont = new Font(Font.HELVETICA, 13, Font.BOLD);

            Paragraph titulo = new Paragraph("Reporte de Stock de Inventario", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);

            Paragraph sub = new Paragraph(
                    "Periodo: " + (periodo == null || periodo.isBlank() ? "N/D" : periodo)
                            + "   |   Generado: " + LocalDateTime.now().format(FECHA_FMT), subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(20);
            doc.add(sub);

            // Resumen estadístico
            doc.add(new Paragraph("Resumen estadístico (stock)", seccionFont));
            PdfPTable resumen = new PdfPTable(2);
            resumen.setWidthPercentage(60);
            resumen.setHorizontalAlignment(Element.ALIGN_LEFT);
            resumen.setSpacingBefore(8);
            resumen.setSpacingAfter(20);
            agregarFilaResumen(resumen, "Cantidad de productos", String.valueOf(stats.getCantidadProductos()));
            agregarFilaResumen(resumen, "Suma total de stock", String.valueOf(stats.getSumaStock()));
            agregarFilaResumen(resumen, "Promedio", String.format("%.2f", stats.getPromedioStock()));
            agregarFilaResumen(resumen, "Mediana", String.format("%.2f", stats.getMedianaStock()));
            agregarFilaResumen(resumen, "Stock máximo", String.valueOf(stats.getStockMaximo()));
            agregarFilaResumen(resumen, "Stock mínimo", String.valueOf(stats.getStockMinimo()));
            doc.add(resumen);

            // Detalle de productos
            doc.add(new Paragraph("Detalle de productos", seccionFont));
            PdfPTable detalle = new PdfPTable(new float[]{2, 5, 3});
            detalle.setWidthPercentage(100);
            detalle.setSpacingBefore(8);
            encabezadoPdf(detalle, "Producto ID", "Nombre", "Stock");
            for (Inventario inv : inventario) {
                detalle.addCell(celdaPdf(String.valueOf(inv.getProductoId())));
                detalle.addCell(celdaPdf(inv.getNombre()));
                detalle.addCell(celdaPdf(String.valueOf(inv.getStock())));
            }
            doc.add(detalle);

            doc.close();
            return new ArchivoReporte("reporte-stock.pdf", "application/pdf", out.toByteArray());
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("Error generando el reporte PDF", e);
        }
    }

    private void agregarFilaResumen(PdfPTable table, String clave, String valor) {
        Font claveFont = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font valorFont = new Font(Font.HELVETICA, 11);
        PdfPCell c1 = new PdfPCell(new Phrase(clave, claveFont));
        c1.setPadding(6);
        c1.setBackgroundColor(new Color(240, 240, 240));
        PdfPCell c2 = new PdfPCell(new Phrase(valor, valorFont));
        c2.setPadding(6);
        table.addCell(c1);
        table.addCell(c2);
    }

    private void encabezadoPdf(PdfPTable table, String... titulos) {
        Font headFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        for (String t : titulos) {
            PdfPCell cell = new PdfPCell(new Phrase(t, headFont));
            cell.setBackgroundColor(new Color(60, 90, 150));
            cell.setPadding(6);
            table.addCell(cell);
        }
    }

    private PdfPCell celdaPdf(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, new Font(Font.HELVETICA, 10)));
        cell.setPadding(5);
        return cell;
    }

    // ─── Excel (Apache POI) ──────────────────────────────────────────────────

    private ArchivoReporte generarExcel(String periodo, EstadisticasStock stats, List<Inventario> inventario) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle titulo = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font tituloFont = wb.createFont();
            tituloFont.setBold(true);
            tituloFont.setFontHeightInPoints((short) 14);
            titulo.setFont(tituloFont);

            CellStyle header = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            header.setFont(headerFont);
            header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Hoja Resumen
            Sheet resumen = wb.createSheet("Resumen");
            int r = 0;
            crearCelda(resumen.createRow(r++), 0, "Reporte de Stock de Inventario", titulo);
            crearCelda(resumen.createRow(r++), 0,
                    "Periodo: " + (periodo == null || periodo.isBlank() ? "N/D" : periodo), null);
            crearCelda(resumen.createRow(r++), 0,
                    "Generado: " + LocalDateTime.now().format(FECHA_FMT), null);
            r++;
            Row encResumen = resumen.createRow(r++);
            crearCelda(encResumen, 0, "Métrica", header);
            crearCelda(encResumen, 1, "Valor", header);
            agregarFilaExcel(resumen, r++, "Cantidad de productos", stats.getCantidadProductos());
            agregarFilaExcel(resumen, r++, "Suma total de stock", stats.getSumaStock());
            agregarFilaExcel(resumen, r++, "Promedio", String.format("%.2f", stats.getPromedioStock()));
            agregarFilaExcel(resumen, r++, "Mediana", String.format("%.2f", stats.getMedianaStock()));
            agregarFilaExcel(resumen, r++, "Stock máximo", stats.getStockMaximo());
            agregarFilaExcel(resumen, r++, "Stock mínimo", stats.getStockMinimo());
            resumen.autoSizeColumn(0);
            resumen.autoSizeColumn(1);

            // Hoja Detalle
            Sheet detalle = wb.createSheet("Detalle");
            Row enc = detalle.createRow(0);
            crearCelda(enc, 0, "Producto ID", header);
            crearCelda(enc, 1, "Nombre", header);
            crearCelda(enc, 2, "Stock", header);
            int fila = 1;
            for (Inventario inv : inventario) {
                Row row = detalle.createRow(fila++);
                row.createCell(0).setCellValue(inv.getProductoId());
                row.createCell(1).setCellValue(inv.getNombre());
                row.createCell(2).setCellValue(inv.getStock());
            }
            detalle.autoSizeColumn(0);
            detalle.autoSizeColumn(1);
            detalle.autoSizeColumn(2);

            wb.write(out);
            return new ArchivoReporte("reporte-stock.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Error generando el reporte Excel", e);
        }
    }

    private void agregarFilaExcel(Sheet sheet, int rowIdx, String metrica, Object valor) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(metrica);
        if (valor instanceof Number n) {
            row.createCell(1).setCellValue(n.doubleValue());
        } else {
            row.createCell(1).setCellValue(String.valueOf(valor));
        }
    }

    private void crearCelda(Row row, int col, String valor, CellStyle estilo) {
        Cell cell = row.createCell(col);
        cell.setCellValue(valor);
        if (estilo != null) cell.setCellStyle(estilo);
    }
}
