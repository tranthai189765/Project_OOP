package filehandler;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileHandler {

    // Đọc các phần tử từ file và lưu vào Set
    public static Set<String> readElementsFromFile(String filePath) {
        Set<String> links = new HashSet<>();
        File file = new File(filePath);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    links.add(line.trim());
                }
            } catch (IOException e) {
                System.out.println("Lỗi khi đọc file " + filePath);
                e.printStackTrace();
            }
        }
        return links;
    }

    // Ghi một chuỗi vào file
    public static void writeStringToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(content);
            writer.newLine();
            System.out.println("Đã ghi nội dung vào file: " + filePath);
        } catch (IOException e) {
            System.out.println("Lỗi khi ghi nội dung vào file " + filePath);
            e.printStackTrace();
        }
    }

    // Ghi danh sách chuỗi vào file
    public static void writeListToFile(String filePath, Set<String> content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (String element : content) {
                writer.write(element);
                writer.newLine();
            }
            System.out.println("Đã ghi danh sách nội dung vào file: " + filePath);
        } catch (IOException e) {
            System.out.println("Lỗi khi ghi danh sách nội dung vào file " + filePath);
            e.printStackTrace();
        }
    }

    // Chia file thành n phần nhỏ
    public static List<String> splitFile(String inputFile, int n) throws IOException {
        List<String> lines = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        if (n <= 0 || lines.size() < n) {
            throw new IllegalArgumentException("Số lượng file con phải nhỏ hơn hoặc bằng số dòng trong file gốc và lớn hơn 0.");
        }

        int linesPerFile = lines.size() / n;
        int extraLines = lines.size() % n;
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        List<String> filePaths = new ArrayList<>();
        int currentIndex = 0;

        for (int i = 0; i < n; i++) {
            int currentFileLines = linesPerFile + (i < extraLines ? 1 : 0);
            List<String> subLines = lines.subList(currentIndex, currentIndex + currentFileLines);
            String outputFileName = timestamp + "_subfilefrom_" + new File(inputFile).getName();
            outputFileName = outputFileName.replace(".txt", "_part" + (i + 1) + ".txt");
            File outputFile = new File(outputFileName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                for (String subLine : subLines) {
                    writer.write(subLine);
                    writer.newLine();
                }
            }

            filePaths.add(outputFile.getAbsolutePath());
            currentIndex += currentFileLines;
        }

        return filePaths;
    }

    // Lấy thông tin đăng nhập từ file
    public static Map<String, String> getCredentialsFromFile(String filePath) {
        Map<String, String> credentials = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("username:")) {
                    credentials.put("username", line.substring("username:".length()).trim());
                } else if (line.startsWith("password:")) {
                    credentials.put("password", line.substring("password:".length()).trim());
                } else if (line.startsWith("email:")) {
                    credentials.put("email", line.substring("email:".length()).trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
        }

        return credentials;
    }

    // Loại bỏ các dòng trùng lặp trong file
    public static void removeDuplicatesInPlace(String filePath) throws IOException {
        Set<String> uniqueLines = new LinkedHashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                uniqueLines.add(line.trim());
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            for (String line : uniqueLines) {
                writer.write(line);
                writer.newLine();
            }
        }

        System.out.println("Đã loại bỏ các dòng trùng lặp trực tiếp trên file: " + filePath);
    }
}
