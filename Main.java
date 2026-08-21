import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    static class MonthData {
        double totalAll = 0.0;
        double totalFiltered = 0.0;
        int removedCount = 0;
        double removedSum = 0.0;
    }

    public static void main(String[] args) {
        String filePath = "report.txt";
        Map<String, MonthData> data2024 = new TreeMap<>();
        Map<String, MonthData> data2025 = new TreeMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";", -1);
                if (fields.length < 16) continue;
                String code = fields[3].trim();
                if (!code.equals("11")) continue;
                try {
                    LocalDate date = LocalDate.parse(fields[1].trim(), DATE_FORMAT);
                    int year = date.getYear();
                    if (year != 2024 && year != 2025) continue;
                    String monthKey = String.format("%02d", date.getMonthValue());
                    double amount = Double.parseDouble(fields[15].trim().replace(',', '.'));

                    Map<String, MonthData> target = (year == 2024) ? data2024 : data2025;
                    MonthData md = target.computeIfAbsent(monthKey, k -> new MonthData());

                    md.totalAll += amount;
                    if (amount >= 10000) {
                        md.removedCount++;
                        md.removedSum += amount;
                    } else {
                        md.totalFiltered += amount;
                    }
                } catch (Exception e) {
                    // skip invalid lines
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            return;
        }

        // Сохраняем в CSV
        saveToCSV(data2024, "revenue_2024.csv", "2024");
        saveToCSV(data2025, "revenue_2025.csv", "2025");

        System.out.println("CSV файлы сохранены: revenue_2024.csv и revenue_2025.csv");
    }

    private static void saveToCSV(Map<String, MonthData> data, String filename, String year) {
        String[] monthNames = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                               "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Месяц;Выручка_без_больших_чеков");
            for (int i = 1; i <= 12; i++) {
                String key = String.format("%02d", i);
                MonthData md = data.get(key);
                if (md != null) {
                    writer.printf("%s;%.2f%n", monthNames[i-1], md.totalFiltered);
                } else {
                    writer.printf("%s;0.00%n", monthNames[i-1]);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения CSV для " + year + ": " + e.getMessage());
        }
    }
}
