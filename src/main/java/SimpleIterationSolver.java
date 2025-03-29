import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

public class SimpleIterationSolver {
    private static final int MAX_ITERATIONS = 10000;
    private final BigDecimal[][] matrix;
    private final int size;
    private final BigDecimal epsilon;
    private final MathContext mc;

    public SimpleIterationSolver(BigDecimal[][] matrix, MathContext mc, BigDecimal epsilon) {
        this.matrix = deepCopy(matrix);
        this.size = matrix.length;
        this.epsilon = epsilon;
        this.mc = mc;
        validateMatrix();
        validateDiagonalDominance();
    }

    public BigDecimal[] solve() {
        BigDecimal[][] C = prepareIterationMatrix();
        BigDecimal[] d = prepareConstantsVector();

        System.out.println("\nНорма матрицы C (∞-норма): " + calculateMatrixNorm(C));
        return performIterations(C, d);
    }

    private BigDecimal calculateMatrixNorm(BigDecimal[][] matrix) {
        BigDecimal norm = BigDecimal.ZERO;
        for (BigDecimal[] row : matrix) {
            BigDecimal rowSum = BigDecimal.ZERO;
            for (BigDecimal element : row) {
                rowSum = rowSum.add(element.abs());
            }
            if (rowSum.compareTo(norm) > 0) {
                norm = rowSum;
            }
        }
        return norm;
    }

    private void validateMatrix() {
        if (matrix == null || matrix.length == 0 || matrix[0].length != matrix.length + 1) {
            throw new IllegalArgumentException("Матрица должна быть размером N x (N+1)");
        }
    }

    private void validateDiagonalDominance() {
        for (int i = 0; i < size; i++) {
            BigDecimal diag = matrix[i][i].abs();
            BigDecimal sum = BigDecimal.ZERO;

            for (int j = 0; j < size; j++) {
                if (j != i) {
                    sum = sum.add(matrix[i][j].abs());
                }
            }

            if (diag.compareTo(sum) <= 0) {
                throw new IllegalStateException("Матрица не обладает диагональным преобладанием");
            }
        }
    }

    private BigDecimal[][] prepareIterationMatrix() {
        BigDecimal[][] C = new BigDecimal[size][size];
        for (int i = 0; i < size; i++) {
            BigDecimal aii = matrix[i][i];
            for (int j = 0; j < size; j++) {
                C[i][j] = (i != j) ? matrix[i][j].negate().divide(aii, mc) : BigDecimal.ZERO;
            }
        }
        return C;
    }

    private BigDecimal[] prepareConstantsVector() {
        BigDecimal[] d = new BigDecimal[size];
        for (int i = 0; i < size; i++) {
            d[i] = matrix[i][size].divide(matrix[i][i], mc);
        }
        return d;
    }

    private BigDecimal[] performIterations(BigDecimal[][] C, BigDecimal[] d) {
        BigDecimal[] current = Arrays.copyOf(d, size);
        BigDecimal[] previous = new BigDecimal[size];
        int iterations = 0;
        BigDecimal error;
        BigDecimal[] errors = new BigDecimal[MAX_ITERATIONS];

        do {
            System.arraycopy(current, 0, previous, 0, size);
            current = addVectors(matrixVectorMultiply(C, previous), d);
            error = maxAbsoluteError(previous, current);
            errors[iterations] = error;
            iterations++;
        } while (error.compareTo(epsilon) > 0 && iterations < MAX_ITERATIONS);

        checkConvergence(iterations, error);
        printSolutionInfo(current, previous, iterations, error, errors);
        return current;
    }

    private BigDecimal[] matrixVectorMultiply(BigDecimal[][] matrix, BigDecimal[] vector) {
        BigDecimal[] result = new BigDecimal[size];
        Arrays.fill(result, BigDecimal.ZERO);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i] = result[i].add(matrix[i][j].multiply(vector[j], mc), mc);
            }
        }
        return result;
    }

    private BigDecimal[] addVectors(BigDecimal[] a, BigDecimal[] b) {
        BigDecimal[] result = new BigDecimal[size];
        for (int i = 0; i < size; i++) {
            result[i] = a[i].add(b[i], mc);
        }
        return result;
    }

    private BigDecimal maxAbsoluteError(BigDecimal[] a, BigDecimal[] b) {
        BigDecimal max = BigDecimal.ZERO;
        for (int i = 0; i < size; i++) {
            BigDecimal diff = a[i].subtract(b[i]).abs();
            if (diff.compareTo(max) > 0) {
                max = diff;
            }
        }
        return max;
    }

    private void checkConvergence(int iterations, BigDecimal error) {
        if (iterations >= MAX_ITERATIONS) {
            throw new RuntimeException(
                    String.format("Метод не сошелся за %d итераций (достигнутая точность: %s)",
                            MAX_ITERATIONS, error.toString())
            );
        }
    }

    private void printSolutionInfo(BigDecimal[] solution, BigDecimal[] lastIteration,
                                   int iterations, BigDecimal finalError, BigDecimal[] errors) {
        System.out.println("\n=== Результаты решения ===");
        System.out.println("Количество итераций: " + iterations);
        System.out.println("Достигнутая точность: " + finalError);

        System.out.println("\nВектор неизвестных:");
        for (int i = 0; i < solution.length; i++) {
            System.out.printf("x%d = %s%n", i+1, solution[i].toString());
        }

        System.out.println("\nВектор погрешностей по итерациям (макс. норма):");
        for (int i = 0; i < iterations; i++) {
            System.out.printf("Итерация %d: %s%n", i+1, errors[i].toString());
        }

        if (iterations > 1) {
            System.out.println("\nВектор погрешностей (последняя - предпоследняя итерация):");
            for (int i = 0; i < solution.length; i++) {
                BigDecimal diff = solution[i].subtract(lastIteration[i]);
                System.out.printf("Δx%d = %s%n", i+1, diff.toString());
            }
        }
    }

    private static BigDecimal[][] deepCopy(BigDecimal[][] matrix) {
        return Arrays.stream(matrix).map(BigDecimal[]::clone).toArray(BigDecimal[][]::new);
    }
}