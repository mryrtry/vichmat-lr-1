import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.NoSuchElementException;
import java.util.Random;

public class InputManager {
    private final BufferedReader reader;
    private final boolean fromFile;
    private static final MathContext MC = new MathContext(50, RoundingMode.HALF_UP);
    private static final Random random = new Random();
    private static final BigDecimal RANGE = new BigDecimal("10");

    public InputManager(InputStream inputStream) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        this.fromFile = inputStream != System.in;
    }

    private BigDecimal[][] _getMatrix(int size) throws IOException {
        BigDecimal[][] matrix = new BigDecimal[size][size + 1];

        for (int i = 0; i < size; i++) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Неожиданный конец ввода: ожидалось " + size + " строк");
            }

            String[] tokens = line.trim().split("\\s+");
            if (tokens.length != size + 1) {
                throw new IOException(
                        "Некорректное количество элементов в строке " + (i+1) +
                                ": ожидалось " + (size+1) + ", получено " + tokens.length
                );
            }

            for (int j = 0; j < size + 1; j++) {
                try {
                    matrix[i][j] = new BigDecimal(tokens[j], MC);
                } catch (NumberFormatException e) {
                    throw new IOException(
                            "Некорректный числовой формат в строке " + (i+1) +
                                    ", столбце " + (j+1) + ": '" + tokens[j] + "'"
                    );
                }
            }
        }
        return matrix;
    }

    public int getMatrixSize() {
        while (true) {
            System.out.println("Введите кол-во уравнений в системе (1-20):");
            try {
                String input = reader.readLine();
                if (input == null) throw new IOException("Неожиданный конец ввода");

                int size = Integer.parseInt(input.trim());
                if (size < 1 || size > 20) throw new IllegalArgumentException();
                return size;
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    public BigDecimal getAccuracy() {
        while (true) {
            System.out.println("Введите необходимую точность:");
            try {
                String input = reader.readLine();
                if (input == null) throw new IOException("Неожиданный конец ввода");

                BigDecimal accuracy = new BigDecimal(input.trim(), MC);
                if (accuracy.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException();
                return accuracy;
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    public BigDecimal[][] getMatrix(int size) {
        try {
            return _getMatrix(size);
        } catch (IOException e) {
            System.err.println("Ошибка при чтении матрицы: " + e.getMessage());
            if (fromFile) System.exit(0);
            return getMatrix(size);
        }
    }

    public static BigDecimal[][] getRandomMatrix(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Размер матрицы должен быть положительным");
        }

        BigDecimal[][] matrix = new BigDecimal[size][size + 1];

        for (int i = 0; i < size; i++) {
            // Генерируем диагональный элемент с запасом для преобладания
            BigDecimal diagonal = getRandomValue()
                    .add(RANGE.multiply(new BigDecimal(size - 1), MC), MC)
                    .abs();

            matrix[i][i] = diagonal;

            // Генерируем недиагональные элементы
            BigDecimal rowSum = BigDecimal.ZERO;
            for (int j = 0; j < size; j++) {
                if (j != i) {
                    matrix[i][j] = getRandomValue();
                    rowSum = rowSum.add(matrix[i][j].abs(), MC);
                }
            }

            // Генерируем свободный член
            matrix[i][size] = getRandomValue();

            // Гарантируем диагональное преобладание
            if (diagonal.compareTo(rowSum) <= 0) {
                // Увеличиваем диагональный элемент, если нужно
                matrix[i][i] = rowSum.add(getRandomValue().abs().add(BigDecimal.ONE), MC);
            }
        }

        return matrix;
    }

    private static BigDecimal getRandomValue() {
        return new BigDecimal(random.nextDouble() * RANGE.doubleValue() * 2 - RANGE.doubleValue(), MC);
    }

    private void handleError(Exception e) {
        if (e instanceof IOException) {
            System.err.println("Ошибка ввода: " + e.getMessage());
        } else if (e instanceof NumberFormatException) {
            System.err.println("Принимается только число, повторите попытку ввода.");
        } else if (e instanceof IllegalArgumentException) {
            System.err.println("Некорректное значение, повторите попытку ввода.");
        } else if (e instanceof  NoSuchElementException) {
            System.err.println("Экстренный выход, завершение программы.");
            System.exit(0);
        }
        if (fromFile) System.exit(0);
    }

    public void close() throws IOException {
        reader.close();
    }
}