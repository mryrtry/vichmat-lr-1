import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        InputStream inputStream = getInputKind();
        boolean fromFile = inputStream != System.in;
        boolean isRandomGenerated = false;
        BigDecimal[][] matrix;
        InputManager inputManager = new InputManager(inputStream);
        int size = inputManager.getMatrixSize();
        BigDecimal accuracy = inputManager.getAccuracy();
        if (!fromFile) {
            isRandomGenerated = getMatrixInputKind();
        }
        if (isRandomGenerated) {
            matrix = inputManager.getRandomMatrix(size);
        } else {
            matrix = inputManager.getMatrix(size);
        }

        System.out.println("Введённая матрица:");
        System.out.println(Arrays.deepToString(matrix));

        BigDecimal[][] diagonalDominanceMatrix = MatrixManager.checkDiagonalDominance(matrix);

        if (diagonalDominanceMatrix == null) {
            System.err.println("Найти решение невозможно, возврат.");
            return;
        }

        BigDecimal[] solve = new SimpleIterationSolver(diagonalDominanceMatrix, new MathContext(50, RoundingMode.HALF_UP), accuracy).solve();
    }

    private static InputStream getInputKind() {
        Scanner scanner = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("Выберите тип ввода:");
            System.out.println("1. Ввод с клавиатуры");
            System.out.println("2. Ввод из файла");

            try {
                choice = Integer.parseInt(scanner.next());
                if (choice == 2) {
                    System.out.println("Введите название файла:");
                    String fileName = scanner.next();
                    return new FileInputStream(fileName);
                } else if (choice == 1) {
                    return System.in;
                }
                throw new IllegalArgumentException();
            } catch (NumberFormatException e) {
                System.err.println("Принимается только число, повторите попытку ввода.");
            } catch (IllegalArgumentException e) {
                System.err.println("Принимается только '1' или '2', повторите попытку ввода.");
            } catch (FileNotFoundException e) {
                System.err.println("Файл не существует или не может быть открыт, повторите попытку ввода.");
            } catch (NoSuchElementException e) {
                System.err.println("Экстренный выход.");
                System.exit(0);
            }
        }
    }

    private static boolean getMatrixInputKind() {
        Scanner scanner = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("Выберите тип ввода:");
            System.out.println("1. Ввод значений");
            System.out.println("2. Случайная генерация");

            try {
                choice = Integer.parseInt(scanner.next());
                if (choice != 1 & choice != 2) throw new IllegalArgumentException();
                return choice == 2;
            } catch (NumberFormatException e) {
                System.err.println("Принимается только число, повторите попытку ввода.");
            } catch (IllegalArgumentException e) {
                System.err.println("Принимается только '1' или '2', повторите попытку ввода.");
            } catch (NoSuchElementException e) {
                System.err.println("Экстренный выход.");
                System.exit(0);
            }
        }
    }
}