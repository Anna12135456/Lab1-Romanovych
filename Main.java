
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Програма для аналізу результатів матчів спортивної ліги.
 */
public class Main {

    /**
     * Точка входу в програму.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        Path input = Path.of("data", "input.csv");
        Path output = Path.of("data", "report.txt");

        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--help":
                        System.out.println(
                            "Використання: java Main "
                            + "[--input файл] [--output файл]");
                        return;
                    case "--version":
                        System.out.println("Версія 1.0.0");
                        return;
                    case "--input":
                        if (++i >= args.length) {
                            throw new IllegalArgumentException(
                                "Не вказано вхідний файл.");
                        }
                        input = Path.of(args[i]);
                        break;
                    case "--output":
                        if (++i >= args.length) {
                            throw new IllegalArgumentException(
                                "Не вказано вихідний файл.");
                        }
                        output = Path.of(args[i]);
                        break;
                    default:
                        throw new IllegalArgumentException(
                            "Невідомий аргумент: " + args[i]);
                }
            }

            List<String> lines = Files.readAllLines(
                input, StandardCharsets.UTF_8);

            int matches = 0;
            int totalGoals = 0;
            int totalAttendance = 0;
            int maxAttendance = 0;

            StringBuilder report = new StringBuilder();
            report.append("ЗВІТ: СПОРТИВНА ЛІГА")
                  .append(System.lineSeparator());

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(";", -1);

                String error = validate(fields);
                if (error != null) {
                    System.err.printf(
                        "Рядок %d пропущено: %s%n",
                        i + 1, error);
                    continue;
                }

                try {
                    int homeScore =
                        Integer.parseInt(fields[2].trim());
                    int awayScore =
                        Integer.parseInt(fields[3].trim());
                    int attendance =
                        Integer.parseInt(fields[4].trim());

                    totalGoals += homeScore + awayScore;
                    totalAttendance += attendance;
                    maxAttendance =
                        Math.max(maxAttendance, attendance);
                    matches++;

                } catch (NumberFormatException e) {
                    System.err.printf(
                        "Рядок %d пропущено: число поза допустимим діапазоном.%n",
                        i + 1);
                }
            }

            double averageGoals = matches == 0
                ? 0.0 : (double) totalGoals / matches;

            report.append("Коректних матчів: ")
                  .append(matches)
                  .append(System.lineSeparator());

            report.append(String.format(
                Locale.ROOT,
                "Середня кількість голів за матч: %.2f%n",
                averageGoals));

            report.append("Найбільша відвідуваність: ")
                  .append(maxAttendance)
                  .append(System.lineSeparator());

            report.append("Сумарна відвідуваність: ")
                  .append(totalAttendance)
                  .append(System.lineSeparator());

            System.out.print(report);

            Path parent = output.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                output, report.toString(),
                StandardCharsets.UTF_8);

            System.out.println("Звіт збережено: " + output);

        } catch (IOException e) {
            System.err.println(
                "Помилка роботи з файлом: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println(
                "Помилка аргументів: " + e.getMessage());
        }
    }

    /**
     * Перевіряє поля запису матчу.
     *
     * @param fields поля рядка CSV
     * @return причина помилки або null для коректного запису
     */
    private static String validate(String[] fields) {
        if (fields.length != 5) {
            return "потрібно рівно 5 полів.";
        }

        if (fields[0].trim().isEmpty()
                || fields[1].trim().isEmpty()) {
            return "назва команди не може бути порожньою.";
        }

        try {
            int home = Integer.parseInt(fields[2].trim());
            int away = Integer.parseInt(fields[3].trim());
            int attendance = Integer.parseInt(fields[4].trim());

            if (home < 0 || away < 0 || attendance < 0) {
                return "числові значення не можуть бути від'ємними.";
            }
        } catch (NumberFormatException e) {
            return "рахунок і відвідуваність мають бути цілими числами.";
        }

        return null;
    }
}
