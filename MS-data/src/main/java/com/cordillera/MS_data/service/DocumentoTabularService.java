package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.ArchivoReporte;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera documentos tabulares (PDF, CSV, XLSX) a partir de un título,
 * una lista de columnas y una lista de filas. Reutilizable para reportes y KPI.
 */
@Service
public class DocumentoTabularService {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Genera los tres formatos (PDF, CSV, XLSX) para el mismo contenido. */
    public List<ArchivoReporte> generarTodos(String titulo, List<String> columnas, List<List<String>> filas) {
        String base = nombreBase(titulo);
        return List.of(
                generarPdf(titulo, columnas, filas, base),
                generarCsv(columnas, filas, base),
                generarXlsx(titulo, columnas, filas, base)
        );
    }

    // ─── PDF ─────────────────────────────────────────────────────────────────

    private ArchivoReporte generarPdf(String titulo, List<String> columnas, List<List<String>> filas, String base) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font tituloFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font subFont = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.DARK_GRAY);

            Paragraph tit = new Paragraph(titulo == null ? "Reporte" : titulo, tituloFont);
            tit.setAlignment(Element.ALIGN_CENTER);
            doc.add(tit);

            Paragraph sub = new Paragraph("Generado: " + LocalDateTime.now().format(FECHA_FMT), subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(18);
            doc.add(sub);

            if (columnas != null && !columnas.isEmpty()) {
                int numCols = columnas.size();
                PdfPTable table = new PdfPTable(numCols);
                table.setWidthPercentage(100);

                Font headFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
                for (String col : columnas) {
                    PdfPCell cell = new PdfPCell(new Phrase(col, headFont));
                    cell.setBackgroundColor(new Color(60, 90, 150));
                    cell.setPadding(6);
                    table.addCell(cell);
                }

                Font cellFont = new Font(Font.HELVETICA, 10);
                if (filas != null) {
                    for (List<String> fila : filas) {
                        for (int i = 0; i < numCols; i++) {
                            String valor = i < fila.size() && fila.get(i) != null ? fila.get(i) : "";
                            PdfPCell cell = new PdfPCell(new Phrase(valor, cellFont));
                            cell.setPadding(5);
                            table.addCell(cell);
                        }
                    }
                }
                doc.add(table);
            }

            doc.close();
            return new ArchivoReporte(base + ".pdf", "application/pdf", out.toByteArray());
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("Error generando el PDF", e);
        }
    }

    // ─── CSV ─────────────────────────────────────────────────────────────────

    private ArchivoReporte generarCsv(List<String> columnas, List<List<String>> filas, String base) {
        StringBuilder sb = new StringBuilder();
        if (columnas != null) {
            sb.append(columnas.stream().map(this::csvEscape).reduce((a, b) -> a + "," + b).orElse(""));
            sb.append("\r\n");
        }
        if (filas != null) {
            for (List<String> fila : filas) {
                sb.append(fila.stream().map(this::csvEscape).reduce((a, b) -> a + "," + b).orElse(""));
                sb.append("\r\n");
            }
        }
        // BOM para que Excel reconozca UTF-8 con acentos
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] texto = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] contenido = new byte[bom.length + texto.length];
        System.arraycopy(bom, 0, contenido, 0, bom.length);
        System.arraycopy(texto, 0, contenido, bom.length, texto.length);
        return new ArchivoReporte(base + ".csv", "text/csv", contenido);
    }

    private String csvEscape(String valor) {
        String v = valor == null ? "" : valor;
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    // ─── XLSX ────────────────────────────────────────────────────────────────

    private ArchivoReporte generarXlsx(String titulo, List<String> columnas, List<List<String>> filas, String base) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Reporte");

            CellStyle header = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            header.setFont(headerFont);
            header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int r = 0;
            if (columnas != null && !columnas.isEmpty()) {
                Row enc = sheet.createRow(r++);
                for (int i = 0; i < columnas.size(); i++) {
                    Cell c = enc.createCell(i);
                    c.setCellValue(columnas.get(i));
                    c.setCellStyle(header);
                }
            }
            if (filas != null) {
                for (List<String> fila : filas) {
                    Row row = sheet.createRow(r++);
                    for (int i = 0; i < fila.size(); i++) {
                        row.createCell(i).setCellValue(fila.get(i) == null ? "" : fila.get(i));
                    }
                }
            }
            int cols = columnas == null ? 0 : columnas.size();
            for (int i = 0; i < cols; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return new ArchivoReporte(base + ".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Error generando el XLSX", e);
        }
    }

    // ─── util ────────────────────────────────────────────────────────────────

    private String nombreBase(String titulo) {
        if (titulo == null || titulo.isBlank()) return "reporte";
        String limpio = titulo.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return limpio.isBlank() ? "reporte" : limpio;
    }
}
