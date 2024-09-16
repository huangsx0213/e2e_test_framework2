import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileExporter {
    // 设置输出文件名
    private static final String OUTPUT_FILE = "output.txt";

    // 设置要导出的根目录
    private static final String ROOT_DIRECTORY = ".";

    // 设置要排除的目录
    private static final Set<String> EXCLUDED_DIRS = new HashSet<>(Arrays.asList(
            "target",
            ".idea",
            ".git"
    ));

    // 设置要排除的文件
    private static final Set<String> EXCLUDED_FILES = new HashSet<>(Arrays.asList(
            "FileExporter.java",
            "pom.xml",
            "README.md",
            ".gitignore"
    ));

    // 设置要包含的文件后缀
    private static final Set<String> INCLUDED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "java", "json", "xml", "yaml", "properties", "feature", "ftl"
    ));

    public static void main(String[] args) {
        FileExporter exporter = new FileExporter();
        exporter.export();
    }

    public void export() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
            writer.write("File Structure:\n");
            exportDirectory(new File(ROOT_DIRECTORY), "", writer);
            writer.write("\nFile Contents:\n");
            exportContents(new File(ROOT_DIRECTORY), writer);
            System.out.println("Export completed. Check " + OUTPUT_FILE + " for results.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportDirectory(File dir, String indent, BufferedWriter writer) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                if (!EXCLUDED_DIRS.contains(file.getName())) {
                    writer.write(indent + "+ " + file.getName() + "\n");
                    exportDirectory(file, indent + "  ", writer);
                }
            } else {
                if (!EXCLUDED_FILES.contains(file.getName()) &&
                        (INCLUDED_EXTENSIONS.isEmpty() || INCLUDED_EXTENSIONS.contains(getFileExtension(file)))) {
                    writer.write(indent + "- " + file.getName() + "\n");
                }
            }
        }
    }

    private void exportContents(File dir, BufferedWriter writer) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                if (!EXCLUDED_DIRS.contains(file.getName())) {
                    exportContents(file, writer);
                }
            } else {
                if (!EXCLUDED_FILES.contains(file.getName()) &&
                        (INCLUDED_EXTENSIONS.isEmpty() || INCLUDED_EXTENSIONS.contains(getFileExtension(file)))) {
                    writer.write("\n--- " + file.getPath() + " ---\n");
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line + "\n");
                        }
                    }
                }
            }
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }
}