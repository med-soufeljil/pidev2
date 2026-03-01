package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight PDF writer with no external dependencies.
 * Generates a single-page PDF with plain text lines.
 */
public final class SimplePdfExporter {

    private SimplePdfExporter() {
    }

    public static void writeSimpleReport(Path path, String title, List<String> lines) throws IOException {
        List<String> allLines = new ArrayList<>();
        allLines.add(title);
        allLines.addAll(lines);

        String content = buildTextContent(allLines);

        List<Integer> offsets = new ArrayList<>();
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");

        offsets.add(pdf.length());
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] ")
                .append("/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        offsets.add(pdf.length());
        byte[] contentBytes = content.getBytes(StandardCharsets.US_ASCII);
        pdf.append("5 0 obj\n<< /Length ").append(contentBytes.length).append(" >>\nstream\n")
                .append(content)
                .append("\nendstream\nendobj\n");

        int xrefOffset = pdf.length();
        pdf.append("xref\n0 6\n");
        pdf.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }

        pdf.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n")
                .append(xrefOffset)
                .append("\n%%EOF\n");

        Files.writeString(path, pdf.toString(), StandardCharsets.US_ASCII);
    }

    private static String buildTextContent(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("BT\n/F1 14 Tf\n50 780 Td\n");

        for (int i = 0; i < lines.size(); i++) {
            String line = escapePdfText(lines.get(i));
            if (i == 0) {
                sb.append("(").append(line).append(") Tj\n");
                sb.append("/F1 11 Tf\n");
            } else {
                sb.append("0 -20 Td\n(").append(line).append(") Tj\n");
            }
        }

        sb.append("ET");
        return sb.toString();
    }

    private static String escapePdfText(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }
}
