import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Genere RAPPORT.pdf a partir de RAPPORT.md, sans dependance externe. */
public class GenerateReportPdf {

    private static final Charset WIN = Charset.forName("windows-1252");

    public static void main(String[] args) throws Exception {
        Path root = Paths.get(".").toAbsolutePath().normalize();
        String source = new String(Files.readAllBytes(root.resolve("RAPPORT.md")), StandardCharsets.UTF_8);

        List<String> lines = wrap(source.split("\\R"));
        final int perPage = 48;
        int pageCount = Math.max(1, (lines.size() + perPage - 1) / perPage);

        int nextId = 4;
        int[] pageIds = new int[pageCount];
        int[] contentIds = new int[pageCount];
        for (int i = 0; i < pageCount; i++) {
            pageIds[i] = nextId++;
            contentIds[i] = nextId++;
        }

        Map<Integer, byte[]> objects = new LinkedHashMap<>();
        objects.put(1, ascii("<< /Type /Catalog /Pages 2 0 R >>"));

        StringBuilder kids = new StringBuilder();
        for (int id : pageIds) {
            kids.append(id).append(" 0 R ");
        }
        objects.put(2, ascii("<< /Type /Pages /Kids [" + kids.toString().trim()
                + "] /Count " + pageCount + " >>"));
        objects.put(3, ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica"
                + " /Encoding /WinAnsiEncoding >>"));

        for (int i = 0; i < pageCount; i++) {
            objects.put(pageIds[i], ascii("<< /Type /Page /Parent 2 0 R"
                    + " /MediaBox [0 0 595 842]"
                    + " /Resources << /Font << /F1 3 0 R >> >>"
                    + " /Contents " + contentIds[i] + " 0 R >>"));

            StringBuilder stream = new StringBuilder("BT\n/F1 11 Tf\n50 792 Td\n14 TL\n");
            int from = i * perPage;
            int to = Math.min(lines.size(), from + perPage);
            for (int j = from; j < to; j++) {
                stream.append('(').append(escape(lines.get(j))).append(") Tj\nT*\n");
            }
            stream.append("ET\n");
            byte[] data = stream.toString().getBytes(WIN);
            byte[] content = concat(
                    ascii("<< /Length " + data.length + " >>\nstream\n"),
                    data,
                    ascii("\nendstream"));
            objects.put(contentIds[i], content);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(ascii("%PDF-1.4\n"));
        Map<Integer, Integer> offsets = new HashMap<>();
        for (Map.Entry<Integer, byte[]> entry : objects.entrySet()) {
            offsets.put(entry.getKey(), out.size());
            out.write(ascii(entry.getKey() + " 0 obj\n"));
            out.write(entry.getValue());
            out.write(ascii("\nendobj\n"));
        }

        int xrefPosition = out.size();
        int size = Collections.max(objects.keySet()) + 1;
        out.write(ascii("xref\n0 " + size + "\n"));
        out.write(ascii("0000000000 65535 f \n"));
        for (int id = 1; id < size; id++) {
            out.write(ascii(String.format("%010d 00000 n \n", offsets.get(id))));
        }
        out.write(ascii("trailer\n<< /Size " + size + " /Root 1 0 R >>\n"
                + "startxref\n" + xrefPosition + "\n%%EOF\n"));

        Files.write(root.resolve("RAPPORT.pdf"), out.toByteArray());
        System.out.println("RAPPORT.pdf genere : " + out.size() + " octets, "
                + pageCount + " page(s)");
    }

    private static List<String> wrap(String[] rawLines) {
        List<String> result = new ArrayList<>();
        final int max = 100;
        for (String raw : rawLines) {
            String line = raw.trim()
                    .replaceAll("^#+\\s*", "")
                    .replace("**", "")
                    .replace("`", "");
            if (line.startsWith("|")) {
                line = line.replace("|", " ").trim();
            }
            if (line.isEmpty()) {
                result.add("");
                continue;
            }
            while (line.length() > max) {
                int cut = line.lastIndexOf(' ', max);
                if (cut < 1) {
                    cut = max;
                }
                result.add(line.substring(0, cut));
                line = line.substring(cut).trim();
            }
            result.add(line);
        }
        return result;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
