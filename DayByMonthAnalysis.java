package org.example;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DayByMonthAnalysis {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void main(String[] args) {
        String filePath = "report.txt";
        Map<LocalDate, Double> dailyRevenue = new HashMap<>();

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
                    double amount = Double.parseDouble(fields[15].trim().replace(',', '.'));
                    if (amount >= 10000) continue;
                    dailyRevenue.put(date, dailyRevenue.getOrDefault(date, 0.0) + amount);
                } catch (Exception e) {
                    // пропускаем некорректные строки
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            return;
        }

        Map<String, Double> sumByKey = new HashMap<>();
        Map<String, Integer> countByKey = new HashMap<>();

        for (Map.Entry<LocalDate, Double> entry : dailyRevenue.entrySet()) {
            LocalDate date = entry.getKey();
            double dayRevenue = entry.getValue();
            int year = date.getYear();
            int month = date.getMonthValue();
            int dow = date.getDayOfWeek().getValue();
            String key = String.format("%d-%02d-%d", year, month, dow);
            sumByKey.put(key, sumByKey.getOrDefault(key, 0.0) + dayRevenue);
            countByKey.put(key, countByKey.getOrDefault(key, 0) + 1);
        }

        saveAvgToCSV(sumByKey, countByKey, "day_month_2024_avg_correct.csv", "2024");
        saveAvgToCSV(sumByKey, countByKey, "day_month_2025_avg_correct.csv", "2025");
        System.out.println("CSV созданы: day_month_2024_avg_correct.csv и day_month_2025_avg_correct.csv");
    }

    private static void saveAvgToCSV(Map<String, Double> sum, Map<String, Integer> count,
                                     String filename, String yearLabel) {
        String[] monthNames = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                               "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        String[] dayNames = {"ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС"};

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Год;Месяц;День_недели;Средняя_выручка_за_день");
            for (int month = 1; month <= 12; month++) {
                for (int dow = 1; dow <= 7; dow++) {
                    String key = String.format("%s-%02d-%d", yearLabel, month, dow);
                    double total = sum.getOrDefault(key, 0.0);
                    int cnt = count.getOrDefault(key, 0);
                    double avg = (cnt > 0) ? total / cnt : 0.0;
                    writer.printf("%s;%s;%s;%.2f%n",
                            yearLabel, monthNames[month-1], dayNames[dow-1], avg);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения CSV: " + e.getMessage());
        }
    }
}
