import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

public class MatrixManager {
    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    public static BigDecimal[][] checkDiagonalDominance(BigDecimal[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix[0].length != matrix.length + 1) {
            System.err.println("Некорректные размеры матрицы");
            return null;
        }

        // Особый случай для матрицы 1x2
        if (matrix.length == 1) {
            if (matrix[0][0].compareTo(BigDecimal.ZERO) == 0) {
                System.err.println("Нулевой диагональный элемент для матрицы 1x1");
                return null;
            }
            return copyMatrix(matrix); // Просто возвращаем копию, так как преобладание тривиально
        }

        int[] rowPermutation = findDominantPermutation(matrix);
        if (rowPermutation == null) {
            System.err.println("Невозможно создать диагональное преобладание");
            return null;
        }

        return reorderMatrix(matrix, rowPermutation);
    }

    private static int[] findDominantPermutation(BigDecimal[][] matrix) {
        int size = matrix.length;

        // Особый случай для матрицы 1x2
        if (size == 1) {
            return new int[]{0}; // Единственная возможная перестановка
        }

        int[] permutation = new int[size];
        boolean[] usedColumns = new boolean[size];

        for (int row = 0; row < size; row++) {
            int bestCol = -1;
            BigDecimal maxRatio = BigDecimal.ZERO;

            for (int col = 0; col < size; col++) {
                if (usedColumns[col]) continue;

                BigDecimal diagonal = matrix[row][col].abs();
                BigDecimal sum = BigDecimal.ZERO;
                boolean allZeros = true;

                for (int k = 0; k < size; k++) {
                    if (k != col) {
                        BigDecimal absVal = matrix[row][k].abs();
                        sum = sum.add(absVal, MC);
                        if (absVal.compareTo(BigDecimal.ZERO) != 0) {
                            allZeros = false;
                        }
                    }
                }

                // Обработка случая, когда все недиагональные элементы нулевые
                if (allZeros) {
                    bestCol = col;
                    break;
                }

                // Проверка деления на ноль
                if (sum.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal ratio = diagonal.divide(sum, MC);
                    if (ratio.compareTo(maxRatio) > 0) {
                        maxRatio = ratio;
                        bestCol = col;
                    }
                }
            }

            if (bestCol == -1) {
                return null;
            }

            permutation[row] = bestCol;
            usedColumns[bestCol] = true;
        }

        int[] rowOrder = new int[size];
        for (int i = 0; i < size; i++) {
            rowOrder[permutation[i]] = i;
        }

        return rowOrder;
    }

    private static BigDecimal[][] reorderMatrix(BigDecimal[][] matrix, int[] rowOrder) {
        int size = matrix.length;
        BigDecimal[][] newMatrix = new BigDecimal[size][size + 1];

        for (int i = 0; i < size; i++) {
            System.arraycopy(matrix[rowOrder[i]], 0, newMatrix[i], 0, size + 1);
        }

        return newMatrix;
    }

    public static BigDecimal[][] copyMatrix(BigDecimal[][] source) {
        return Arrays.stream(source)
                .map(BigDecimal[]::clone)
                .toArray(BigDecimal[][]::new);
    }
}