package com.my.ai.cursor.common.utils;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * PdfUtils
 *
 * @author 刘强
 * @version 2026/04/08 15:08
 **/
public class PdfUtils {

    private static final Logger logger = LoggerFactory.getLogger(PdfUtils.class);

    private static final String TESSDATA_PATH = "D:\\Program Files\\Tesseract-OCR\\tessdata";

    /**
     * 将 PDF 转为 Markdown
     */
    public static String convertToMarkdown(byte[] pdfBytes) {
        return extractText(pdfBytes).text();
    }

    /**
     * 将本地 PDF 文件转为 Markdown
     */
    public static String convertToMarkdown(File pdfFile) {
        try (var document = PDDocument.load(pdfFile)) {
            return extractText(document).text();
        } catch (IOException e) {
            throw new RuntimeException("PDF 读取失败", e);
        }
    }

    public static PdfParseResult extractText(byte[] pdfBytes) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            return extractText(document);
        } catch (IOException e) {
            throw new RuntimeException("PDF 读取失败", e);
        }
    }

    private static PdfParseResult extractText(PDDocument document) {
        var renderer = new PDFRenderer(document);
        int totalPages = document.getNumberOfPages();
        logger.info("开始解析 PDF 共 {} 页...", totalPages);
        String text = IntStream.range(0, totalPages)
            .mapToObj(pageIndex -> processSinglePage(document, renderer, pageIndex))
            .filter(pageContent -> !pageContent.isBlank())
            .collect(Collectors.joining("\n\n---\n\n"));
        return new PdfParseResult(totalPages, text);
    }

    private static String processSinglePage(PDDocument document, PDFRenderer renderer, int pageIndex) {
        String text = extractNativeText(document, pageIndex);
        text = cleanChineseText(text);

        if (text.trim().length() < 50) {
            logger.warn("[第 {} 页] 内容较少，使用 OCR 解析...", pageIndex + 1);
            text = performOcrIfNecessary(renderer, pageIndex, text);
        }

        return text.trim().isEmpty() ? "" : "### 第 %d 页\n\n%s".formatted(pageIndex + 1, text.trim());
    }

    private static String performOcrIfNecessary(PDFRenderer renderer, int pageIndex, String currentText) {
        try {
            BufferedImage image = renderer.renderImageWithDPI(pageIndex, 300);
            String ocrText = ocrImage(image);
            return cleanChineseText(ocrText);
        } catch (IOException e) {
            logger.error("[第 {} 页] OCR 渲染失败: {}", pageIndex + 1, e.getMessage());
            return currentText;
        }
    }

    private static String extractNativeText(PDDocument document, int pageIndex) {
        try {
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void processTextPosition(TextPosition text) {
                    super.processTextPosition(text);
                }
            };
            stripper.setStartPage(pageIndex + 1);
            stripper.setEndPage(pageIndex + 1);
            return stripper.getText(document);
        } catch (IOException e) {
            logger.error("[第 {} 页] 原生文本提取失败: {}", pageIndex + 1, e.getMessage());
            return "";
        }
    }

    public static String cleanChineseText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text
            .replaceAll("(?<=\\p{IsHan})\\s+(?=\\p{IsHan})", "")
            .replaceAll("(?<=\\p{IsHan})(?=[a-zA-Z0-9])", " ")
            .replaceAll("(?<=[a-zA-Z0-9])(?=\\p{IsHan})", " ")
            .replaceAll("[\\[\\{\\(\\)\\}\\]（）【】\"“”]", "")
            .replaceAll("(?<=\\d)\\s*(kcal|mmHg|g|mg|μg|%)", " $1")
            .replaceAll("\\s*([，。！？；：、])\\s*", "$1")
            .replaceAll("[^\\p{IsHan}\\p{IsAlphabetic}\\p{Digit}\\p{Punct}\\s]", "")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private static String ocrImage(BufferedImage image) {
        try {
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(TESSDATA_PATH);
            tesseract.setLanguage("chi_sim");
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);

            String ocrText = tesseract.doOCR(image);
            return ocrText.replaceAll("录目", "目录");
        } catch (TesseractException e) {
            logger.error("OCR 识别失败: {}", e.getMessage());
            return "";
        }
    }

    public record PdfParseResult(int pageCount, String text) {
    }
}
