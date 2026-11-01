package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.LiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.dto.RegistrarLiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.dto.ResumenLiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.model.Carga;
import com.utvt.ApiSpringCafeSoft.model.EntregaPedido;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Liquidacion;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.model.Venta;
import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.EntregaPedidoRepository;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.LiquidacionRepository;
import com.utvt.ApiSpringCafeSoft.repository.MovimientoPuntosRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;

/**
 * HU-016: resumen y cierre de turno de repartidores.
 */


@Service
public class LiquidacionService {

    @Autowired private LiquidacionRepository liquidacionRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CargaRepository cargaRepository;
    @Autowired private EntregaPedidoRepository entregaPedidoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private MovimientoPuntosRepository movimientoPuntosRepository;


    public ResumenLiquidacionDTO obtenerResumen(Long repartidorId, LocalDate fecha) {
        Usuario repartidor = obtenerRepartidor(repartidorId);
        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();

        LocalDateTime inicio = fechaConsulta.atStartOfDay();
        LocalDateTime fin = fechaConsulta.plusDays(1).atStartOfDay();

        List<Carga> cargas = cargaRepository
                .findByRepartidorIdAndFechaHoraBetweenOrderByFechaHoraAsc(
                        repartidorId,
                        inicio,
                        fin
                );

        List<EntregaPedido> entregas = entregaPedidoRepository
                .findByRepartidorIdAndFechaBetweenOrderByFechaAsc(
                        repartidorId,
                        inicio,
                        fin
                );

        double cargaInicial = cargas.stream()
                .mapToDouble(c -> nvl(c.getCantidad()))
                .sum();

        List<EntregaPedido> entregasCompletadas = entregas.stream()
                .filter(e -> "ENTREGADO".equalsIgnoreCase(e.getResultado()))
                .toList();

        double garrafonesEntregados = entregasCompletadas.stream()
                .mapToDouble(e -> nvl(e.getGarrafonesEntregados()))
                .sum();

        double envasesVacios = entregasCompletadas.stream()
                .mapToDouble(e -> nvl(e.getEnvasesVaciosRecibidos()))
                .sum();

        double efectivo = entregasCompletadas.stream()
                .filter(e -> "EFECTIVO".equalsIgnoreCase(e.getMetodoCobro()))
                .mapToDouble(e -> nvl(e.getMontoCobrado()))
                .sum();

        double transferencias = entregasCompletadas.stream()
                .filter(e -> "TRANSFERENCIA".equalsIgnoreCase(e.getMetodoCobro()))
                .mapToDouble(e -> nvl(e.getMontoCobrado()))
                .sum();

        ResumenLiquidacionDTO dto = new ResumenLiquidacionDTO();
        dto.setRepartidorId(repartidor.getId());
        dto.setRepartidorNombre(repartidor.getNombre());
        dto.setFecha(fechaConsulta);
        dto.setCargaInicial(cargaInicial);
        dto.setGarrafonesEntregados(garrafonesEntregados);
        dto.setGarrafonesPendientes(Math.max(0.0, cargaInicial - garrafonesEntregados));
        dto.setEnvasesVaciosRecibidos(envasesVacios);
        dto.setTotalEfectivo(efectivo);
        dto.setTotalTransferencias(transferencias);
        dto.setTotalCobrado(efectivo + transferencias);
        dto.setEntregasRealizadas(entregasCompletadas.size());
        return dto;
    }


    public List<LiquidacionDTO> listarLiquidaciones(Long repartidorId) {
        Usuario repartidor = obtenerRepartidor(repartidorId);
        return liquidacionRepository
                .findByRepartidorIdOrderByFechaOperacionDesc(repartidor.getId())
                .stream()
                .map(this::convertirDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public LiquidacionDTO obtenerLiquidacionHoy(Long repartidorId) {
        Usuario repartidor = obtenerRepartidor(repartidorId);
        LocalDate hoy = LocalDate.now();
        List<Liquidacion> lista = liquidacionRepository
                .findByRepartidorIdAndFechaOperacionOrderByFechaCierreDesc(repartidor.getId(), hoy);
        if (lista.isEmpty()) return null;
        return convertirDTO(lista.get(0));
    }

public byte[] generarPdfLiquidacion(Long repartidorId, LocalDate fecha) {
    Usuario repartidor = obtenerRepartidor(repartidorId);
    LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
    LocalDateTime inicio = fechaConsulta.atStartOfDay();
    LocalDateTime fin = fechaConsulta.plusDays(1).atStartOfDay();

    List<Liquidacion> liqs = liquidacionRepository
            .findByRepartidorIdAndFechaOperacionOrderByFechaCierreDesc(repartidorId, fechaConsulta);
    Liquidacion liquidacion = liqs.isEmpty() ? null : liqs.get(0);

    List<EntregaPedido> entregas = entregaPedidoRepository
            .findByRepartidorIdAndFechaBetweenOrderByFechaAsc(repartidorId, inicio, fin);

    List<EntregaPedido> entregasCompletadas = entregas.stream()
            .filter(e -> "ENTREGADO".equalsIgnoreCase(e.getResultado()))
            .toList();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document document = new Document(com.lowagie.text.PageSize.A4, 40, 40, 60, 40);
    try {
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        com.lowagie.text.pdf.PdfContentByte canvas = writer.getDirectContentUnder();

        // ── MARCA DE AGUA (logo) ──
        try {
            byte[] logoBytes = getClass().getClassLoader()
                    .getResourceAsStream("logo_aquasoft.png")
                    .readAllBytes();
            com.lowagie.text.Image logoWm = com.lowagie.text.Image.getInstance(logoBytes);
            float pageW = document.getPageSize().getWidth();
            float pageH = document.getPageSize().getHeight();
            logoWm.scaleToFit(300, 300);
            logoWm.setAbsolutePosition(
                    (pageW - logoWm.getScaledWidth()) / 2f,
                    (pageH - logoWm.getScaledHeight()) / 2f
            );
            canvas.saveState();
            com.lowagie.text.pdf.PdfGState gs = new com.lowagie.text.pdf.PdfGState();
            gs.setFillOpacity(0.07f);
            canvas.setGState(gs);
            canvas.addImage(logoWm);
            canvas.restoreState();
        } catch (Exception ignored) {
            // Si no encuentra el logo, continúa sin marca de agua
        }

        // ── FUENTES ──
        com.lowagie.text.Font fuenteTitulo = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 18,
                com.lowagie.text.Font.BOLD,
                new java.awt.Color(7, 85, 107));
        com.lowagie.text.Font fuenteSubtitulo = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 11,
                com.lowagie.text.Font.NORMAL,
                new java.awt.Color(80, 80, 80));
        com.lowagie.text.Font fuenteSeccion = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 10,
                com.lowagie.text.Font.BOLD,
                new java.awt.Color(7, 85, 107));
        com.lowagie.text.Font fuenteNormal = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 9,
                com.lowagie.text.Font.NORMAL,
                new java.awt.Color(40, 40, 40));
        com.lowagie.text.Font fuenteNormalBold = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 9,
                com.lowagie.text.Font.BOLD,
                new java.awt.Color(40, 40, 40));
        com.lowagie.text.Font fuenteTablaHeader = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 8,
                com.lowagie.text.Font.BOLD,
                java.awt.Color.WHITE);
        com.lowagie.text.Font fuenteTabla = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 8,
                com.lowagie.text.Font.NORMAL,
                new java.awt.Color(40, 40, 40));

        java.awt.Color colorPrimario = new java.awt.Color(7, 85, 107);
        java.awt.Color colorFondoFila = new java.awt.Color(230, 248, 250);
        java.awt.Color colorBorde = new java.awt.Color(180, 220, 230);

        // ── ENCABEZADO ──
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{65f, 35f});
        headerTable.setSpacingAfter(10f);

        // Logo en encabezado
        com.lowagie.text.pdf.PdfPCell cellLogo;
        try {
            byte[] logoBytes2 = getClass().getClassLoader()
                    .getResourceAsStream("logo_aquasoft.png")
                    .readAllBytes();
            com.lowagie.text.Image logoHeader = com.lowagie.text.Image.getInstance(logoBytes2);
            logoHeader.scaleToFit(90, 40);
            cellLogo = new com.lowagie.text.pdf.PdfPCell(logoHeader);
        } catch (Exception e2) {
            cellLogo = new com.lowagie.text.pdf.PdfPCell(
                    new Paragraph("AquaSoft", fuenteTitulo));
        }
        cellLogo.setBorder(com.lowagie.text.pdf.PdfPCell.NO_BORDER);
        cellLogo.setVerticalAlignment(com.lowagie.text.pdf.PdfPCell.ALIGN_MIDDLE);
        headerTable.addCell(cellLogo);

        // Info del documento
        com.lowagie.text.pdf.PdfPCell cellInfo = new com.lowagie.text.pdf.PdfPCell();
        cellInfo.setBorder(com.lowagie.text.pdf.PdfPCell.NO_BORDER);
        cellInfo.setHorizontalAlignment(com.lowagie.text.pdf.PdfPCell.ALIGN_RIGHT);
        cellInfo.addElement(new Paragraph("REGISTRO DE LIQUIDACIÓN", fuenteTitulo));
        cellInfo.addElement(new Paragraph("AquaSoft · Sistema de Gestión", fuenteSubtitulo));
        cellInfo.addElement(new Paragraph(
                "Fecha: " + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").format(fechaConsulta),
                fuenteSubtitulo));
        headerTable.addCell(cellInfo);
        document.add(headerTable);

        // Línea separadora
        com.lowagie.text.pdf.PdfPTable lineaTop = new com.lowagie.text.pdf.PdfPTable(1);
        lineaTop.setWidthPercentage(100);
        com.lowagie.text.pdf.PdfPCell lineaCell = new com.lowagie.text.pdf.PdfPCell(new Paragraph(" "));
        lineaCell.setBackgroundColor(colorPrimario);
        lineaCell.setFixedHeight(3f);
        lineaCell.setBorder(0);
        lineaTop.addCell(lineaCell);
        document.add(lineaTop);
        document.add(new Paragraph(" ", fuenteNormal));

        // ── DATOS DEL REPARTIDOR ──
        document.add(new Paragraph("DATOS DEL REPARTIDOR", fuenteSeccion));
        document.add(new Paragraph(" ", new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 3)));

        PdfPTable datosTable = new PdfPTable(4);
        datosTable.setWidthPercentage(100);
        datosTable.setSpacingAfter(12f);

        agregarCeldaDato(datosTable, "Nombre", repartidor.getNombre(), fuenteNormal, fuenteNormalBold, colorFondoFila);
        agregarCeldaDato(datosTable, "Correo", repartidor.getEmail() != null ? repartidor.getEmail() : "—", fuenteNormal, fuenteNormalBold, colorFondoFila);
        agregarCeldaDato(datosTable, "Turno", fechaConsulta.toString(), fuenteNormal, fuenteNormalBold, colorFondoFila);
        agregarCeldaDato(datosTable, "Estado liquidación",
                liquidacion != null ? liquidacion.getEstado() : "SIN CIERRE",
                fuenteNormal, fuenteNormalBold, colorFondoFila);
        document.add(datosTable);

        // ── RESUMEN DE JORNADA ──
        document.add(new Paragraph("RESUMEN DE JORNADA", fuenteSeccion));
        document.add(new Paragraph(" ", new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 3)));

        PdfPTable resumenTable = new PdfPTable(3);
        resumenTable.setWidthPercentage(100);
        resumenTable.setSpacingAfter(12f);

        String cargaIni = liquidacion != null ? String.format("%.0f", liquidacion.getCargaInicial()) : "N/A";
        String garEntregados = liquidacion != null ? String.format("%.0f", liquidacion.getGarrafonesEntregados()) : "N/A";
        String garDevueltos = liquidacion != null ? String.format("%.0f", liquidacion.getGarrafonesNoVendidosDevueltos()) : "N/A";
        String totEfectivo = liquidacion != null ? String.format("$%.2f", liquidacion.getTotalEfectivo()) : "N/A";
        String totTransfer = liquidacion != null ? String.format("$%.2f", liquidacion.getTotalTransferencias()) : "N/A";
        String efEntregado = liquidacion != null ? String.format("$%.2f", liquidacion.getEfectivoEntregado()) : "N/A";
        String difGarrafon = liquidacion != null ? String.format("%.0f", liquidacion.getDiferenciaGarrafones()) : "N/A";
        String difEfectivo = liquidacion != null ? String.format("$%.2f", liquidacion.getDiferenciaEfectivo()) : "N/A";

        agregarCeldaResumen(resumenTable, "🚰 Carga inicial", cargaIni + " garrafones", fuenteNormal, fuenteNormalBold, colorFondoFila, colorBorde);
        agregarCeldaResumen(resumenTable, "✅ Garrafones entregados", garEntregados, fuenteNormal, fuenteNormalBold, colorFondoFila, colorBorde);
        agregarCeldaResumen(resumenTable, "↩️ Garrafones devueltos", garDevueltos, fuenteNormal, fuenteNormalBold, colorFondoFila, colorBorde);
        agregarCeldaResumen(resumenTable, "💵 Total efectivo cobrado", totEfectivo, fuenteNormal, fuenteNormalBold, colorFondoFila, colorBorde);
        agregarCeldaResumen(resumenTable, "🏦 Total transferencias", totTransfer, fuenteNormal, fuenteNormalBold, colorFondoFila, colorBorde);
        agregarCeldaResumen(resumenTable, "💰 Efectivo entregado", efEntregado, fuenteNormal, fuenteNormalBold, colorFondoFila, colorBorde);
        agregarCeldaResumen(resumenTable, "Δ Diferencia garrafones", difGarrafon, fuenteNormal, fuenteNormalBold, colorFondoFila, colorBorde);
        agregarCeldaResumen(resumenTable, "Δ Diferencia efectivo", difEfectivo, fuenteNormal, fuenteNormalBold, colorFondoFila, colorBorde);
        agregarCeldaResumen(resumenTable, "📦 Entregas realizadas", String.valueOf(entregasCompletadas.size()), fuenteNormal, fuenteNormalBold, colorFondoFila, colorBorde);
        document.add(resumenTable);

        // ── DETALLE DE VENTAS ──
        document.add(new Paragraph("DETALLE DE VENTAS POR CLIENTE", fuenteSeccion));
        document.add(new Paragraph(" ", new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 3)));

        // Encabezados tabla de ventas
        PdfPTable ventasTable = new PdfPTable(9);
        ventasTable.setWidthPercentage(100);
        ventasTable.setWidths(new float[]{9f, 14f, 10f, 9f, 10f, 11f, 9f, 10f, 18f});
        ventasTable.setSpacingAfter(10f);

        String[] headers = {
            "Folio", "Cliente", "Garrafones", "Precio/u",
            "Total cobrado", "Forma de pago", "Desc. aplicado",
            "Envases vacíos", "Uso de puntos / Promo"
        };
        for (String h : headers) {
            com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new Paragraph(h, fuenteTablaHeader));
            cell.setBackgroundColor(colorPrimario);
            cell.setPadding(5f);
            cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            cell.setBorderColor(colorBorde);
            ventasTable.addCell(cell);
        }

        double totalGeneral = 0;
        int numFila = 0;
        for (EntregaPedido e : entregasCompletadas) {
            Venta v = e.getVenta();
            java.awt.Color fondoFila = (numFila % 2 == 0)
                    ? java.awt.Color.WHITE
                    : colorFondoFila;

            String folio = v != null ? v.getFolio() : "—";
            String cliente = v != null && v.getNombreCliente() != null ? v.getNombreCliente() : "—";
            String garrafones = String.format("%.0f", nvl(e.getGarrafonesEntregados()));
            double descuento = v != null && v.getDescuento() != null ? v.getDescuento() : 0.0;
            double montoCobrado = nvl(e.getMontoCobrado());
            double garrafonesNum = nvl(e.getGarrafonesEntregados());
            double precioUnitario = (garrafonesNum > 0) ? (montoCobrado + descuento) / garrafonesNum : 0.0;
            String formaPago = e.getMetodoCobro() != null ? e.getMetodoCobro() : "—";
            String vaciosStr = String.format("%.0f", nvl(e.getEnvasesVaciosRecibidos()));

        // Determinar origen del descuento:
        // 1. Puntos canjeados → existe MovimientoPuntos negativo para esta venta
        // 2. Promoción        → hay descuento pero no hay canje de puntos
        // 3. Sin descuento    → "—"
        String puntoPromo = "—";
        if (v != null) {
        if ("PUNTOS".equalsIgnoreCase(v.getMetodoPago())) {
                // Venta de mostrador pagada íntegramente con puntos
                puntoPromo = "Pago con puntos";
        } else if (descuento > 0) {
                // Hay descuento: distinguir si fue por puntos o por promo
                boolean fuePorPuntos = movimientoPuntosRepository
                        .findByVentaId(v.getId())
                        .map(m -> m.getPuntos() != null && m.getPuntos() < 0)
                        .orElse(false);
                if (fuePorPuntos) {
                int puntosCanjeados = movimientoPuntosRepository
                        .findByVentaId(v.getId())
                        .map(m -> Math.abs(m.getPuntos()))
                        .orElse(0);
                puntoPromo = String.format("Puntos canjeados: %d (Desc. $%.2f)",
                        puntosCanjeados, descuento);
                } else {
                // Descuento por promoción — el modelo Venta no guarda el nombre
                // de la promo, solo el monto. Se muestra el monto descontado.
                puntoPromo = String.format("Promoción aplicada (Desc. $%.2f)", descuento);
                }
        }
        }


            agregarCeldaTabla(ventasTable, folio, fuenteTabla, fondoFila, colorBorde, com.lowagie.text.Element.ALIGN_CENTER);
            agregarCeldaTabla(ventasTable, cliente, fuenteTabla, fondoFila, colorBorde, com.lowagie.text.Element.ALIGN_LEFT);
            agregarCeldaTabla(ventasTable, garrafones, fuenteTabla, fondoFila, colorBorde, com.lowagie.text.Element.ALIGN_CENTER);
            agregarCeldaTabla(ventasTable, String.format("$%.2f", precioUnitario), fuenteTabla, fondoFila, colorBorde, com.lowagie.text.Element.ALIGN_CENTER);
            agregarCeldaTabla(ventasTable, String.format("$%.2f", montoCobrado), fuenteTabla, fondoFila, colorBorde, com.lowagie.text.Element.ALIGN_CENTER);
            agregarCeldaTabla(ventasTable, formaPago, fuenteTabla, fondoFila, colorBorde, com.lowagie.text.Element.ALIGN_CENTER);
            agregarCeldaTabla(ventasTable, String.format("$%.2f", descuento), fuenteTabla, fondoFila, colorBorde, com.lowagie.text.Element.ALIGN_CENTER);
            agregarCeldaTabla(ventasTable, vaciosStr, fuenteTabla, fondoFila, colorBorde, com.lowagie.text.Element.ALIGN_CENTER);
            agregarCeldaTabla(ventasTable, puntoPromo, fuenteTabla, fondoFila, colorBorde, com.lowagie.text.Element.ALIGN_LEFT);

            totalGeneral += montoCobrado;
            numFila++;
        }

        if (entregasCompletadas.isEmpty()) {
            com.lowagie.text.pdf.PdfPCell sinVentas = new com.lowagie.text.pdf.PdfPCell(
                    new Paragraph("Sin ventas registradas en este turno.", fuenteTabla));
            sinVentas.setColspan(9);
            sinVentas.setPadding(8f);
            sinVentas.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            ventasTable.addCell(sinVentas);
        }

        document.add(ventasTable);

        // ── TOTAL FINAL ──
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(50);
        totalTable.setHorizontalAlignment(com.lowagie.text.pdf.PdfPTable.ALIGN_RIGHT);
        totalTable.setSpacingAfter(20f);

        com.lowagie.text.pdf.PdfPCell labelTotal = new com.lowagie.text.pdf.PdfPCell(
                new Paragraph("TOTAL GENERAL COBRADO", fuenteTablaHeader));
        labelTotal.setBackgroundColor(colorPrimario);
        labelTotal.setPadding(7f);
        labelTotal.setBorderColor(colorBorde);
        totalTable.addCell(labelTotal);

        com.lowagie.text.Font fuenteTotal = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLD,
                colorPrimario);
        com.lowagie.text.pdf.PdfPCell valorTotal = new com.lowagie.text.pdf.PdfPCell(
                new Paragraph(String.format("$%.2f", totalGeneral), fuenteTotal));
        valorTotal.setBackgroundColor(colorFondoFila);
        valorTotal.setPadding(7f);
        valorTotal.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        valorTotal.setBorderColor(colorBorde);
        totalTable.addCell(valorTotal);
        document.add(totalTable);

        // ── OBSERVACIONES ──
        if (liquidacion != null && liquidacion.getObservaciones() != null
                && !liquidacion.getObservaciones().isBlank()) {
            document.add(new Paragraph("OBSERVACIONES", fuenteSeccion));
            com.lowagie.text.pdf.PdfPTable obsTable = new com.lowagie.text.pdf.PdfPTable(1);
            obsTable.setWidthPercentage(100);
            obsTable.setSpacingAfter(12f);
            com.lowagie.text.pdf.PdfPCell obsCell = new com.lowagie.text.pdf.PdfPCell(
                    new Paragraph(liquidacion.getObservaciones(), fuenteNormal));
            obsCell.setPadding(8f);
            obsCell.setBorderColor(colorBorde);
            obsCell.setBackgroundColor(new java.awt.Color(255, 252, 230));
            obsTable.addCell(obsCell);
            document.add(obsTable);
        }

        // ── PIE DE PÁGINA ──
        com.lowagie.text.pdf.PdfPTable piePage = new com.lowagie.text.pdf.PdfPTable(1);
        piePage.setWidthPercentage(100);
        com.lowagie.text.pdf.PdfPCell pieCell = new com.lowagie.text.pdf.PdfPCell(
                new Paragraph("AquaSoft · Documento generado automáticamente · " + fechaConsulta,
                        new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 7,
                                com.lowagie.text.Font.ITALIC, new java.awt.Color(130, 130, 130))));
        pieCell.setBackgroundColor(colorPrimario);
        pieCell.setPadding(5f);
        pieCell.setBorder(0);
        pieCell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        com.lowagie.text.Font fuentePie = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 7, com.lowagie.text.Font.ITALIC,
                java.awt.Color.WHITE);
        pieCell.setPaddingBottom(5f);
        com.lowagie.text.pdf.PdfPCell pieCell2 = new com.lowagie.text.pdf.PdfPCell(
                new Paragraph("AquaSoft · Documento generado automáticamente · " + fechaConsulta, fuentePie));
        pieCell2.setBackgroundColor(colorPrimario);
        pieCell2.setPadding(6f);
        pieCell2.setBorder(0);
        pieCell2.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        piePage.addCell(pieCell2);
        document.add(piePage);

        document.close();
    } catch (Exception ex) {
        throw new RuntimeException("Error generando PDF: " + ex.getMessage());
    }
    return baos.toByteArray();
}

// ── Helpers para construir celdas del PDF ──

private void agregarCeldaDato(PdfPTable table, String etiqueta, String valor,
        com.lowagie.text.Font fuenteLabel, com.lowagie.text.Font fuenteValor,
        java.awt.Color fondo) {
    com.lowagie.text.pdf.PdfPCell c = new com.lowagie.text.pdf.PdfPCell();
    c.addElement(new Paragraph(etiqueta, fuenteLabel));
    c.addElement(new Paragraph(valor, fuenteValor));
    c.setBackgroundColor(fondo);
    c.setPadding(6f);
    c.setBorderColor(new java.awt.Color(200, 220, 230));
    table.addCell(c);
}

private void agregarCeldaResumen(PdfPTable table, String etiqueta, String valor,
        com.lowagie.text.Font fuenteLabel, com.lowagie.text.Font fuenteValor,
        java.awt.Color fondo, java.awt.Color colorBorde) {
    com.lowagie.text.pdf.PdfPCell c = new com.lowagie.text.pdf.PdfPCell();
    c.addElement(new Paragraph(etiqueta, fuenteLabel));
    Paragraph p = new Paragraph(valor, fuenteValor);
    c.addElement(p);
    c.setBackgroundColor(fondo);
    c.setPadding(7f);
    c.setBorderColor(colorBorde);
    table.addCell(c);
}

private void agregarCeldaTabla(PdfPTable table, String texto,
        com.lowagie.text.Font fuente, java.awt.Color fondo,
        java.awt.Color colorBorde, int alineacion) {
    com.lowagie.text.pdf.PdfPCell c = new com.lowagie.text.pdf.PdfPCell(new Paragraph(texto, fuente));
    c.setBackgroundColor(fondo);
    c.setPadding(4f);
    c.setHorizontalAlignment(alineacion);
    c.setBorderColor(colorBorde);
    table.addCell(c);
}

    @Transactional
    public LiquidacionDTO registrarLiquidacion(RegistrarLiquidacionDTO request) {
        LocalDate fecha = request.getFecha() != null
                ? request.getFecha()
                : LocalDate.now();

        Usuario repartidor = obtenerRepartidor(request.getRepartidorId());

        // Se permite re-liquidar el mismo repartidor en el mismo día.
        // Si ya existe una liquidación previa, se registra una nueva
        // (la anterior se conserva como historial).

        ResumenLiquidacionDTO resumen = obtenerResumen(repartidor.getId(), fecha);

        double devueltos = nvl(request.getGarrafonesNoVendidosDevueltos());
        double efectivoEntregado = nvl(request.getEfectivoEntregado());

        if (devueltos - resumen.getGarrafonesPendientes() > 0.0001) {
            throw new RuntimeException(
                    "Los garrafones devueltos no pueden exceder los pendientes. Pendientes: "
                            + resumen.getGarrafonesPendientes()
            );
        }

        double diferenciaGarrafones =
                resumen.getCargaInicial()
                        - resumen.getGarrafonesEntregados()
                        - devueltos;

        double diferenciaEfectivo =
                resumen.getTotalEfectivo()
                        - efectivoEntregado;

        boolean conforme =
                Math.abs(diferenciaGarrafones) < 0.0001
                        && Math.abs(diferenciaEfectivo) < 0.01;

        Liquidacion liquidacion = new Liquidacion();
        liquidacion.setRepartidor(repartidor);
        liquidacion.setFechaOperacion(fecha);
        liquidacion.setFechaCierre(LocalDateTime.now());
        liquidacion.setCargaInicial(resumen.getCargaInicial());
        liquidacion.setGarrafonesEntregados(resumen.getGarrafonesEntregados());
        liquidacion.setGarrafonesNoVendidosDevueltos(devueltos);
        liquidacion.setTotalEfectivo(resumen.getTotalEfectivo());
        liquidacion.setTotalTransferencias(resumen.getTotalTransferencias());
        liquidacion.setEfectivoEntregado(efectivoEntregado);
        liquidacion.setDiferenciaGarrafones(diferenciaGarrafones);
        liquidacion.setDiferenciaEfectivo(diferenciaEfectivo);
        liquidacion.setEstado(conforme ? "CONFORME" : "DIFERENCIA REGISTRADA");
        liquidacion.setObservaciones(request.getObservaciones());

        regresarGarrafonesAPlanta(repartidor.getId(), fecha, devueltos);

        return convertirDTO(liquidacionRepository.save(liquidacion));
    }

    /**
     * Los garrafones que regresan sin vender vuelven al inventario de planta.
     * Las cargas del turno quedan cerradas como LIQUIDADA.
     */
    private void regresarGarrafonesAPlanta(Long repartidorId, LocalDate fecha, double devueltos) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();

        List<Carga> cargas = cargaRepository
                .findByRepartidorIdAndFechaHoraBetweenOrderByFechaHoraAsc(
                        repartidorId,
                        inicio,
                        fin
                );

        double porRegresar = devueltos;

        for (Carga carga : cargas) {
            double disponible = carga.getCantidadDisponible() != null
                    ? carga.getCantidadDisponible()
                    : nvl(carga.getCantidad());

            double regresar = Math.min(disponible, Math.max(0.0, porRegresar));

            if (regresar > 0.0001 && carga.getInventario() != null) {
                Inventario inventario = carga.getInventario();
                inventario.setCantidad(nvl(inventario.getCantidad()) + regresar);
                inventarioRepository.save(inventario);
                porRegresar -= regresar;
                carga.setCantidadDisponible(Math.max(0.0, disponible - regresar));
            }

            carga.setEstado("LIQUIDADA");
            cargaRepository.save(carga);
        }
    }

    private Usuario obtenerRepartidor(Long id) {
        Usuario repartidor = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repartidor no encontrado con ID: " + id));

        if (repartidor.getUserTipo() == null || repartidor.getUserTipo() != 4) {
            throw new RuntimeException("El usuario seleccionado no tiene el rol de Repartidor");
        }

        return repartidor;
    }

    private double nvl(Double value) {
        return value != null ? value : 0.0;
    }

    private LiquidacionDTO convertirDTO(Liquidacion l) {
        LiquidacionDTO dto = new LiquidacionDTO();
        dto.setId(l.getId());
        dto.setRepartidorId(l.getRepartidor() != null ? l.getRepartidor().getId() : null);
        dto.setRepartidorNombre(l.getRepartidor() != null ? l.getRepartidor().getNombre() : null);
        dto.setFechaOperacion(l.getFechaOperacion());
        dto.setFechaCierre(l.getFechaCierre());
        dto.setCargaInicial(l.getCargaInicial());
        dto.setGarrafonesEntregados(l.getGarrafonesEntregados());
        dto.setGarrafonesNoVendidosDevueltos(l.getGarrafonesNoVendidosDevueltos());
        dto.setTotalEfectivo(l.getTotalEfectivo());
        dto.setTotalTransferencias(l.getTotalTransferencias());
        dto.setEfectivoEntregado(l.getEfectivoEntregado());
        dto.setDiferenciaGarrafones(l.getDiferenciaGarrafones());
        dto.setDiferenciaEfectivo(l.getDiferenciaEfectivo());
        dto.setEstado(l.getEstado());
        dto.setObservaciones(l.getObservaciones());
        return dto;
    }
}
