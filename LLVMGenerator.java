import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Stack;

class LLVMGenerator {

    static String header_text = "";
    static String main_text = "";
    static String buffer = "";
    static int reg = 1;
    static int str = 1;
    static int br = 0;

    static Stack<Integer> brstack = new Stack<Integer>();


    static void printf_i32(String id) {
        main_text += "%" + reg + " = load i32, i32* %" + id + "\n";
        reg++;
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_bool(String id) {
        main_text += "%" + reg + " = load i1, i1* %" + id + "\n";
        reg++;
        String formatStr = "%" + reg + " = select i1 %" + (reg - 1) + ", i8* getelementptr inbounds ([6 x i8], [6 x i8]* @true_str, i32 0, i32 0), i8* getelementptr inbounds ([7 x i8], [7 x i8]* @false_str, i32 0, i32 0)\n";
        main_text += formatStr;
        reg++;
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_double(String id) {
        main_text += "%" + reg + " = load double, double* %" + id + "\n";
        reg++;
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_float(String id) {
         main_text += "%" + reg + " = load float, float* %" + id + "\n";
         reg++;
         main_text += "%" + reg + "= fpext float %" + (reg-1) + " to double \n";
         reg++;
         main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (reg - 1) + ")\n";
         reg++;
  }

    static void printf_string(String id) {
        main_text += "%" + reg + " = load i8*, i8** %" + id + "\n";
        reg++;
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strps, i32 0, i32 0), i8* %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_array_i32_element(String id, Integer size, Integer position) {
        main_text += "%" + reg + " = getelementptr inbounds [" + size + " x i32], [" + size + " x i32]* %" + id + ", i32 0, i32 " + position + "\n";
        reg++;
        // Załaduj wartość elementu z obliczonego adresu
        main_text += "%" + reg + " = load i32, i32* %" + (reg - 1) + "\n";
        reg++;
        // Wywołaj funkcję printf, aby wydrukować wartość elementu
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_matrix_i32_element(String id, int rows, int columns, int rowId, int colId) {
        int position = rowId * columns + colId; // Obliczenie pozycji elementu w macierzy

        // Obliczanie adresu elementu macierzy
        main_text += "%" + reg + " = getelementptr inbounds [" + rows + " x [" + columns + " x i32]], [" + rows + " x [" + columns + " x i32]]* %" + id + ", i32 0, i32 0, i32 " + position + "\n";
        reg++;

        // Załaduj wartość elementu z obliczonego adresu
        main_text += "%" + reg + " = load i32, i32* %" + (reg - 1) + "\n";
        reg++;

        // Wywołaj funkcję printf, aby wydrukować wartość elementu
        main_text += "call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_array_i32_from_matrix(String id, int rows, int columns, int rowId) {
        for (int i = 0; i < columns; i++) {
            // Obliczanie adresu elementu kolumny macierzy
            main_text += "%" + reg + " = getelementptr inbounds [" + rows + " x [" + columns + " x i32]], [" + rows + " x [" + columns + " x i32]]* %" + id + ", i32 0, i32 " + rowId + ", i32 " + i + "\n";
            reg++;
            // Załaduj wartość elementu z obliczonego adresu
            printf_array(i,columns);
        }
    }

    static void printf_column_i32_from_matrix(String id, int rows, int columns, int columnId) {
        for (int i = 0; i < rows; i++) {
            // Obliczanie adresu elementu kolumny macierzy
            main_text += "%" + reg + " = getelementptr inbounds [" + rows + " x [" + columns + " x i32]], [" + rows + " x [" + columns + " x i32]]* %" + id + ", i32 0, i32 " + i + ", i32 " + columnId + "\n";
            reg++;
            // Załaduj wartość elementu z obliczonego adresu
            printf_array(i,rows);
        }
    }

    static void printf_array_i32(String id, Integer size) {
        System.err.println("column assign " + size);
        System.err.println("getelementptr inbounds [" + size + " x i32], [" + size + " x i32]* %" + id + ", i32 0, i32 " + 0 + "end");
        for (int i = 0; i < size; i++) {
            main_text += "%" + reg + " = getelementptr inbounds [" + size + " x i32], [" + size + " x i32]* %" + id + ", i32 0, i32 " + i + "\n";
            reg++;
            // Załaduj wartość elementu z obliczonego adresu
            printf_array(i,size);
        }
    }

    static void printf_array(int i,int size){
        System.err.println("print array i: " + i);
        main_text += "%" + reg + " = load i32, i32* %" + (reg - 1) + "\n";
        reg++;
        String formatStr = "[4 x i8], [4 x i8]* @str_format";
        if (i == 0) {
            formatStr = "[5 x i8], [5 x i8]* @str_first_format";
        } else if (i == size - 1) {
            formatStr = "[5 x i8], [5 x i8]* @str_last_format";
        }
        // Wywołaj funkcję printf, aby wydrukować wartość elementu
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds (" + formatStr + ", i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_matrix_i32(String id, int row, int col) {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                // Obliczanie adresu elementu macierzy
                main_text += "%" + reg + " = getelementptr inbounds [" + row + " x [" + col + " x i32]], [" + row + " x [" + col + " x i32]]* %" + id + ", i32 0, i32 " + i + ", i32 " + j + "\n";
                reg++;
                // Załaduj wartość elementu z obliczonego adresu
                main_text += "%" + reg + " = load i32, i32* %" + (reg - 1) + "\n";
                reg++;
                String formatStr = "[4 x i8], [4 x i8]* @str_format";
                if (i == 0 && j == 0) {
                    formatStr = "[5 x i8], [5 x i8]* @str_first_format";
                } else if (i == row - 1 && j == col - 1) {
                    formatStr = "[5 x i8], [5 x i8]* @str_last_format";
                } else if (i != row -1 && j == col - 1) {
                    formatStr = "[5 x i8], [5 x i8]* @str_row_last_format";
                } else if (i != 0 && j == 0) {
                    formatStr = "[5 x i8], [5 x i8]* @str_row_first_format";
                }
                // Wywołaj funkcję printf, aby wydrukować wartość elementu
                main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds (" + formatStr + ", i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
                reg++;
            }
        }
    }

    static void printf_array_double_element(String id, Integer size, Integer position) {
        main_text += "%" + reg + " = getelementptr inbounds [" + size + " x double], [" + size + " x double]* %" + id + ", i32 0, i32 " + position + "\n";
        reg++;
        // Załaduj wartość elementu z obliczonego adresu
        main_text += "%" + reg + " = load double, double* %" + (reg - 1) + "\n";
        reg++;
        // Wywołaj funkcję printf, aby wydrukować wartość elementu
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_array_double(String id, Integer size) {
        for (int i = 0; i < size; i++) {
            main_text += "%" + reg + " = getelementptr inbounds [" + size + " x double], [" + size + " x double]* %" + id + ", i32 0, i32 " + i + "\n";
            reg++;
            // Załaduj wartość elementu z obliczonego adresu
            main_text += "%" + reg + " = load double, double* %" + (reg - 1) + "\n";
            reg++;
            String formatStr = "[4 x i8], [4 x i8]* @str_format_double";
            if (i == 0) {
                formatStr = "[5 x i8], [5 x i8]* @str_first_format_double";
            } else if (i == size - 1) {
                formatStr = "[5 x i8], [5 x i8]* @str_last_format_double";
            }
            // Wywołaj funkcję printf, aby wydrukować wartość elementu
            main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds (" + formatStr + ", i32 0, i32 0), double %" + (reg - 1) + ")\n";
            reg++;

            //main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (reg - 1) + ")\n";

        }
    }


    static void scanf_i32(String id) {
        main_text += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i32* %" + id + ")\n";
        reg++;
    }

    static void scanf_bool(String id) {
        
      //   main_text += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i1* %" + id + ")\n";
      //   reg++;
      main_text += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([5 x i8], [5 x i8]* @boolStr, i32 0, i32 0), i8* getelementptr inbounds ([7 x i8], [7 x i8]* @inputBuffer, i32 0, i32 0))\n";
      reg++;
      main_text += "%" + reg + " = call i32 @strcmp(i8* getelementptr inbounds ([7 x i8], [7 x i8]* @inputBuffer, i32 0, i32 0), i8* getelementptr inbounds ([5 x i8], [5 x i8]* @true_str2, i32 0, i32 0))\n";
      reg++;
      main_text += "%" + reg + " = icmp eq i32 0, %" + (reg - 1) + "\n";
      reg++;
      main_text += "store i1 %" + (reg - 1) + ", i1* %" + id + "\n";

    }

    static void allocate_string(String id, int l) {
        main_text += "%" + id + " = alloca [" + (l + 1) + " x i8]\n";
    }

    static void scanf_double(String id, String val) {
        main_text += "%" + reg + " = alloca double\n";
        int allocaReg = reg;
        reg++;
        main_text += "store double " + val + ", double* %" + id + "\n";
        main_text += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strDouble, i64 0, i64 0), double*  %" + allocaReg + ")\n";
        reg++;
        main_text += "%" + reg + " = load double, double* %" + allocaReg + "\n";
        reg++;
        main_text += "store double %" + (reg - 1) + ", double* %" + id + "\n";
    }

    static void scanf_string(String id, int l) {
        allocate_string("str" + str, l);
        main_text += "%" + reg + " = getelementptr inbounds [" + (l + 1) + " x i8], [" + (l + 1) + " x i8]* %str" + str + ", i64 0, i64 0\n";
        reg++;
        assign_string(id);
        str++;
        main_text += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([5 x i8], [5 x i8]* @strs2, i32 0, i32 0), i8* %" + (reg - 1) + ")\n";
        reg++;
    }

    static void declare_i32(String id) {
        main_text += "%" + id + " = alloca i32\n";
    }

    static void declare_double(String id) {
        main_text += "%" + id + " = alloca double\n";
    }
    
    static void declare_float(String id) {
      main_text += "%" + id + " = alloca float\n";
  }

    static void declare_array_i32(String id, int size) {
        main_text += "%" + id + " = alloca [" + (size) + " x i32]\n";
    }

    static void declare_matrix_i32(String id, int rows,int columns) {
        main_text += "%" + id + " = alloca [" + rows + " x [ " + columns + " x i32 ]]\n";

    }

    static void declare_array_double(String id, int size) {
        main_text += "%" + id + " = alloca [" + size + " x double]\n";
    }

    static void declare_string(String id) {
        main_text += "%" + id + " = alloca i8*\n";
    }

    static void declare_bool(String id) {
        main_text += "%" + id + " = alloca i1\n";
    }

    static void assign_i32(String id, String value) {
        main_text += "store i32 " + value + ", i32* %" + id + "\n";
    }

    static void assign_i32_from_array(String array_id, String destination_id, Integer size, Integer position) {
        main_text += "%" + reg + " = getelementptr inbounds [" + size + " x i32], [" + size + " x i32]* %" + array_id + ", i32 0, i32 " + position + "\n";
        reg++;
        // Załaduj wartość elementu z obliczonego adresu
        main_text += "%" + reg + " = load i32, i32* %" + (reg - 1) + "\n";
        reg++;
        // Zapisz załadowaną wartość do zmiennej docelowej
        main_text += "store i32 %" + (reg - 1) + ", i32* %" + destination_id + "\n";
    }

    static void assign_array_i32_from_matrix(String matrix_id, String destination_id, Integer rowSize, Integer colSize, Integer row){
        for (int col = 0; col < colSize; col++) {
            // Obliczanie adresu elementu macierzy
            main_text += "%" + reg + " = getelementptr inbounds [" + rowSize + " x [" + colSize + " x i32]], [" + rowSize + " x [" + colSize + " x i32]]* %" + matrix_id + ", i32 0, i32 " + row + ", i32 " + col + "\n";
            reg++;

            // Załaduj wartość elementu z obliczonego adresu
            main_text += "%" + reg + " = load i32, i32* %" + (reg - 1) + "\n";
            reg++;

            // Obliczanie adresu w docelowym buforze
            main_text += "%" + reg + " = getelementptr inbounds [" + colSize + " x i32], [" + colSize + " x i32]* %" + destination_id + ", i32 0, i32 " + col + "\n";
            reg++;

            // Zapisz załadowaną wartość do odpowiedniego miejsca w docelowym buforze
            main_text += "store i32 %" + (reg - 2) + ", i32* %" + (reg - 1) + "\n";
        }

    }

    static void assign_column_i32_from_matrix(String matrix_id, String destination_id, Integer rowSize, Integer colSize, Integer column){
        System.err.println("rowSize " + rowSize);
        for (int row = 0; row < rowSize; row++) {
            // Obliczanie adresu elementu macierzy
            main_text += "%" + reg + " = getelementptr inbounds [" + rowSize + " x [" + colSize + " x i32]], [" + rowSize + " x [" + colSize + " x i32]]* %" + matrix_id + ", i32 0, i32 " + row + ", i32 " + column + "\n";
            reg++;

            // Załaduj wartość elementu z obliczonego adresu
            main_text += "%" + reg + " = load i32, i32* %" + (reg - 1) + "\n";
            reg++;

            // Obliczanie adresu w docelowym buforze
            main_text += "%" + reg + " = getelementptr inbounds [" + rowSize + " x i32], [" + rowSize + " x i32]* %" + destination_id + ", i32 0, i32 " + row + "\n";
            reg++;

            // Zapisz załadowaną wartość do odpowiedniego miejsca w docelowym buforze
            main_text += "store i32 %" + (reg - 2) + ", i32* %" + (reg - 1) + "\n";
        }

    }

    static void assign_i32_from_matrix(String matrix_id, String destination_id, Integer rowSize,Integer colSize, Integer row,Integer col) {
        int position = row * colSize + col; // Obliczanie pozycji elementu w macierzy

        // Obliczanie adresu elementu macierzy
        main_text += "%" + reg + " = getelementptr inbounds [" + rowSize + " x [" + colSize + " x i32]], [" + rowSize + " x [" + colSize + " x i32]]* %" + matrix_id + ", i32 0, i32 0, i32 " + position + "\n";
        reg++;

        // Załaduj wartość elementu z obliczonego adresu
        main_text += "%" + reg + " = load i32, i32* %" + (reg - 1) + "\n";
        reg++;

        // Zapisz załadowaną wartość do zmiennej docelowej
        main_text += "store i32 %" + (reg - 1) + ", i32* %" + destination_id + "\n";
    }

    static void assign_array_i32(String id, List<String> values) {

        for (int i = 0; i < values.size(); i++) {
            // Obliczanie adresu elementu wektora
            //String elementPtr = "%" + id + "_elem_" + i;
            main_text += "%" + reg + " = getelementptr inbounds [" + values.size() + " x i32], [" + values.size() + " x i32]* %" + id + ", i32 0, i32 " + i + "\n";
            // Generowanie kodu do przypisania wartości do odpowiednich indeksów wektora
            main_text += "store i32 " + values.get(i) + ", i32* %" + (reg) + "\n";
            reg++;
        }
    }

    static void assign_matrix_i32(String id, int rows, int cols, List<String> values) {
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Obliczanie adresu elementu macierzy
                main_text += "%" + reg + " = getelementptr inbounds [" + rows + " x [" + cols + " x i32]], [" + rows + " x [" + cols + " x i32]]* %" + id + ", i32 0, i32 " + i + ", i32 " + j + "\n";
                // Generowanie kodu do przypisania wartości do odpowiednich indeksów macierzy
                main_text += "store i32 " + values.get(index) + ", i32* %" + reg + "\n";
                reg++;
                index++;
            }
        }
    }

    static void assign_array_i32_element(String id, Integer value, Integer size, Integer position) {
        // Obliczanie adresu elementu wektora
        //String elementPtr = "%" + id + "_elem_" + i;
        main_text += "%" + reg + " = getelementptr inbounds [" + size + " x i32], [" + size + " x i32]* %" + id + ", i32 0, i32 " + position + "\n";
        // Generowanie kodu do przypisania wartości do odpowiednich indeksów wektora
        main_text += "store i32 " + value + ", i32* %" + (reg) + "\n";
        reg++;
    }

    static void assign_array_double_element(String id, String value, Integer size, Integer position) {
        // Obliczanie adresu elementu wektora
        //String elementPtr = "%" + id + "_elem_" + i;
        main_text += "%" + reg + " = getelementptr inbounds [" + size + " x double], [" + size + " x double]* %" + id + ", i32 0, i32 " + position + "\n";
        // Generowanie kodu do przypisania wartości do odpowiednich indeksów wektora
        main_text += "store double " + value + ", double* %" + reg + "\n";
        reg++;
    }

    static void assign_matrix_i32_element(String id, int value, int rowSize, int colSize, int rowId, int colId) {
        int position = rowId * colSize + colId; // Obliczenie pozycji elementu w macierzy

        // Obliczanie adresu elementu macierzy
        main_text += "%" + reg + " = getelementptr inbounds [" + rowSize + " x [" + colSize + " x i32]], [" + rowSize + " x [" + colSize + " x i32]]* %" + id + ", i32 0, i32 0, i32 " + position + "\n";

        // Generowanie kodu do przypisania wartości do odpowiedniego elementu macierzy
        main_text += "store i32 " + value + ", i32* %" + reg + "\n";
        reg++;
    }

    static void assign_array_double(String id, List<String> values) {

        for (int i = 0; i < values.size(); i++) {
            // Obliczanie adresu elementu wektora
            //String elementPtr = "%" + id + "_elem_" + i;
            main_text += "%" + reg + " = getelementptr inbounds [" + values.size() + " x double], [" + values.size() + " x double]* %" + id + ", i32 0, i32 " + i + "\n";
            // Generowanie kodu do przypisania wartości do odpowiednich indeksów wektora
            main_text += "store double " + values.get(i) + ", double* %" + reg + "\n";
            reg++;
        }
    }

    static void assign_double_from_array(String array_id, String destination_id, Integer size, Integer position) {
        main_text += "%" + reg + " = getelementptr inbounds [" + size + " x double], [" + size + " x double]* %" + array_id + ", i32 0, i32 " + position + "\n";
        reg++;
        // Załaduj wartość elementu z obliczonego adresu
        main_text += "%" + reg + " = load double, double* %" + (reg - 1) + "\n";
        reg++;
        // Zapisz załadowaną wartość do zmiennej docelowej
        main_text += "store double %" + (reg - 1) + ", double* %" + destination_id + "\n";

    }

    static void assign_double_from_matrix(String array_id, String destination_id, Integer size, Integer position) {
        main_text += "%" + reg + " = getelementptr inbounds [" + size + " x double], [" + size + " x double]* %" + array_id + ", i32 0, i32 " + position + "\n";
        reg++;
        // Załaduj wartość elementu z obliczonego adresu
        main_text += "%" + reg + " = load double, double* %" + (reg - 1) + "\n";
        reg++;
        // Zapisz załadowaną wartość do zmiennej docelowej
        main_text += "store double %" + (reg - 1) + ", double* %" + destination_id + "\n";

    }

    static void assign_bool(String id, String value) {
        main_text += "store i1 " + value + ", i1* %" + id + "\n";
    }

    static void assign_double(String id, String value) {
        main_text += "store double " + value + ", double* %" + id + "\n";
    }
    
    static void assign_float(String id, String value) {
      main_text += "store float " + value + ", float* %" + id + "\n";
  }

    static void assign_string(String id) {
        main_text += "store i8* %" + (reg - 1) + ", i8** %" + id + "\n";
    }

    static void constant_string(String content) {
        int l = content.length() + 1;
        header_text += "@str" + str + " = constant [" + l + " x i8] c\"" + content + "\\00\"\n";
        String n = "str" + str;
        LLVMGenerator.allocate_string(n, (l - 1));
        main_text += "%" + reg + " = bitcast [" + l + " x i8]* %" + n + " to i8*\n";
        main_text += "call void @llvm.memcpy.p0i8.p0i8.i64(i8* align 1 %" + reg + ", i8* align 1 getelementptr inbounds ([" + l + " x i8], [" + l + " x i8]* @" + n + ", i32 0, i32 0), i64 " + l + ", i1 false)\n";
        reg++;
        main_text += "%ptr" + n + " = alloca i8*\n";
        main_text += "%" + reg + " = getelementptr inbounds [" + l + " x i8], [" + l + " x i8]* %" + n + ", i64 0, i64 0\n";
        reg++;
        main_text += "store i8* %" + (reg - 1) + ", i8** %ptr" + n + "\n";
        str++;
    }

    static void load_i32(String id) {
        main_text += "%" + reg + " = load i32, i32* %" + id + "\n";
        reg++;
    }

    static void load_double(String id) {
        main_text += "%" + reg + " = load double, double* %" + id + "\n";
        reg++;
    }

    static void load_float(String id) {
      main_text += "%" + reg + " = load float, float* %" + id + "\n";
      reg++;
  }

    static void load_string(String id) {
        main_text += "%" + reg + " = load i8*, i8** %" + id + "\n";
        reg++;
    }

    static void load_bool(String id){
         main_text += "%" + reg + " = load i1, i1* %" + id + "\n";
         reg++;
    }

    static void string_pointer(String id, int l) {
        main_text += "%" + reg + " = getelementptr inbounds [" + (l + 1) + " x i8], [" + (l + 1) + " x i8]* %" + id + ", i64 0, i64 0\n";
        reg++;
    }

    static void add_i32(String val1, String val2) {
        main_text += "%" + reg + " = add i32 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void add_double(String val1, String val2) {
        main_text += "%" + reg + " = fadd double " + val1 + ", " + val2 + "\n";
        reg++;
    }
    
    static void add_float(String val1, String val2) {
      main_text += "%" + reg + " = fadd float " + val1 + ", " + val2 + "\n";
      reg++;
  }

    static void add_string(String id1, int l1, String id2, int l2) {
        allocate_string("str" + str, l1 + l2);
        main_text += "%ptrstr" + str + " = alloca i8*\n";
        main_text += "%" + reg + " = getelementptr inbounds [" + (l1 + l2 + 1) + " x i8], [" + (l1 + l2 + 1) + " x i8]* %str" + str + ", i64 0, i64 0\n";
        reg++;
        main_text += "store i8* %" + (reg - 1) + ", i8** %ptrstr" + str + "\n";
        main_text += "%" + reg + " = load i8*, i8** %ptrstr" + str + "\n";
        reg++;
        main_text += "%" + reg + " = call i8* @strcpy(i8* %" + (reg - 1) + ", i8* " + id1 + ")\n";
        reg++;
        main_text += "%" + reg + " = call i8* @strcat(i8* %" + (reg - 2) + ", i8* " + id2 + ")\n";
        reg++;
        str++;
    }

    static void neg_i32(String val2) {
        main_text += "%" + reg + " = sub i32 " + 0 + "," + val2 + "\n";
        reg++;
    }

    static void neg_double(String val2) {
        main_text += "%" + reg + " = fsub double " + -0.0 + ", " + val2 + "\n";
        reg++;
    }

    static void neg_float(String val2) {
      main_text += "%" + reg + " = fsub float " + -0.0 + ", " + val2 + "\n";
      reg++;
  }

    static void sub_i32(String val1, String val2) {
        main_text += "%" + reg + " = sub i32 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void sub_double(String val1, String val2) {
        main_text += "%" + reg + " = fsub double " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void sub_float(String val1, String val2) {
      main_text += "%" + reg + " = fsub float " + val1 + ", " + val2 + "\n";
      reg++;
  }

    static void mult_i32(String val1, String val2) {
        main_text += "%" + reg + " = mul i32 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void mult_double(String val1, String val2) {
        main_text += "%" + reg + " = fmul double " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void mult_float(String val1, String val2) {
      main_text += "%" + reg + " = fmul float " + val1 + "," + val2 + "\n";
      reg++;
  }

    static void div_i32(String val1, String val2) {
        main_text += "%" + reg + " = sdiv i32 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void div_double(String val1, String val2) {
        main_text += "%" + reg + " = fdiv double " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void div_float(String val1, String val2) {
      main_text += "%" + reg + " = fdiv float " + val1 + ", " + val2 + "\n";
      reg++;
  }

    static void log_and(String val1, String val2) {
        main_text += "%" + reg + " = and i1 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void log_or(String val1, String val2) {
        main_text += "%" + reg + " = or i1 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void log_xor(String val1, String val2) {
        main_text += "%" + reg + " = xor i1 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void log_neg(String val1) {
        main_text += "%" + reg + " = icmp eq i1 " + 0 + ", " + val1 + "\n";
        reg++;
    }

    static void sitofp(String id) {
        main_text += "%" + reg + " = sitofp i32 " + id + " to double\n";
        reg++;
    }

    static void fptosi(String id) {
        main_text += "%" + reg + " = fptosi double " + id + " to i32\n";
        reg++;
    }

    static void dobtoflo(String id) {
      main_text += "%" + reg + "= fptrunc double " + id +" to float \n";
      // main_text += "%" + reg + " = call i16 @llvm.convert.to.fp16.f64(double "+ id +")\n";
      // main_text += "store i16 %"+ reg +", i16* @x";
      reg++;
    }

    static void flotodob(String id) {
      main_text += "%" + reg + "= fpext float " + id + " to double \n";
      // main_text += "%" + reg + " = call double @llvm.convert.from.fp16(i16" + id + " )\n";
      reg++;
    }

    static void float_to_int (String id){
      main_text += "%" + reg + "= fptosi float " + id + " to i32 \n";
      reg ++;
    }

    static void int_to_float (String id){
      main_text += "%" + reg + "= sitofp i32 " + id + " to float \n";
      reg ++;
    }

    static void int_to_string(String in, int lout) {
        allocate_string("str" + str, lout);
        main_text += "%ptrstr" + str + " = alloca i8*\n";
        main_text += "%" + reg + " = getelementptr inbounds [" + (lout + 1) + " x i8], [" + (lout + 1) + " x i8]* %str" + str + ", i64 0, i64 0\n";
        reg++;
        main_text += "store i8* %" + (reg - 1) + ", i8** %ptrstr" + str + "\n";
        main_text += "%" + reg + " = load i8*, i8** %ptrstr" + str + "\n";
        reg++;
        str++;
        main_text += "%" + reg + " = call i32 (i8*, i8*, ...) @sprintf(i8* %" + (reg - 1) + ", i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strspi, i32 0, i32 0), i32 " + in + ")\n";
        reg++;
    }

    static void string_to_int(String in) {
        main_text += "%" + reg + " = call i32 @atoi(i8* " + in + ")\n";
        reg++;
    }


    static void icmp(String id, String value){
      main_text += "%"+reg+" = load i32, i32* %"+id+"\n";
      reg++;
      main_text += "%"+reg+" = icmp eq i32 %"+(reg-1)+", "+value+"\n";
      reg++;
    }

    static void ifstart(){
      br++;
      main_text += "br i1 %"+(reg-1)+", label %true"+br+", label %false"+br+"\n";
      main_text += "true"+br+":\n";
      brstack.push(br);
    }

    static void ifend(){
      int b = brstack.pop();
      main_text += "br label %false"+b+"\n";
      main_text += "false"+b+":\n";
    }

    static void startloop(String reps){
      declare_i32(Integer.toString(reg));
      int counter = reg;
      reg++;
      assign_i32(Integer.toString(counter), "0");
      br++;

      main_text += "br label %cond"+br+"\n";
      main_text += "cond"+br+":\n";

      load_i32(Integer.toString(counter));
      add_i32("%"+(reg-1), "1");
      assign_i32(Integer.toString(counter), "%"+(reg-1));

      main_text += "%"+reg+" = icmp slt i32 %"+(reg-2)+", "+reps+"\n";
      reg++;

      main_text += "br i1 %"+(reg-1)+", label %true"+br+", label %false"+br+"\n";
      main_text += "true"+br+":\n";
      brstack.push(br);
    }

    static void endloop(){
      int b = brstack.pop();
      main_text += "br label %cond"+b+"\n";
      main_text += "false"+b+":\n";
    }

    static void close_main(){
      main_text += buffer;
    }


    static String generate() {
        String text = "";
        text += "declare i32 @printf(i8*, ...)\n";
        text += "declare double @llvm.convert.from.fp16.f64(i16 %a)\n";
        text += "declare i16 @llvm.convert.to.fp16.f64(double %a)\n";
        text += "declare i32 @sprintf(i8*, i8*, ...)\n";
        text += "declare i8* @strcpy(i8*, i8*)\n";
        text += "declare i8* @strcat(i8*, i8*)\n";
        text += "declare i32 @strcmp(i8*, i8*)\n";
        text += "declare i32 @atoi(i8*)\n";
        text += "declare i32 @__isoc99_scanf(i8*, ...)\n";
        text += "declare double @double_scanf(i8*, ...)\n";
        text += "declare void @llvm.memcpy.p0i8.p0i8.i64(i8* noalias nocapture writeonly, i8* noalias nocapture readonly, i64, i1 immarg)\n";
        text += "@strpi = constant [4 x i8] c\"%d\\0A\\00\"\n";
        text += "@strpd = constant [4 x i8] c\"%f\\0A\\00\"\n";
        text += "@strs = constant [3 x i8] c\"%d\\00\"\n";
        text += "@strs2 = constant [5 x i8] c\"%10s\\00\"\n";
        text += "@strspi = constant [3 x i8] c\"%d\\00\"\n";
        text += "@str_first_format =  constant [5 x i8] c\"[%d,\\00\"\n";
        text += "@str_row_first_format =  constant [5 x i8] c\" %d,\\00\"\n";
        text += "@str_format =  constant [4 x i8] c\"%d,\\00\"\n";
        text += "@str_row_last_format =  constant [5 x i8] c\"%d;\\0A\\00\"\n";
        text += "@str_last_format =  constant [5 x i8] c\"%d]\\0A\\00\"\n";
        text += "@str_first_format_double =  constant [5 x i8] c\"[%f,\\00\"\n";
        text += "@str_format_double =  constant [4 x i8] c\"%f,\\00\"\n";
        text += "@str_last_format_double =  constant [5 x i8] c\"%f]\\0A\\00\"\n";
        text += "@.strDouble = private constant [4 x i8] c\"%lf\00\"\n";
        text += "@strps = constant [4 x i8] c\"%s\\0A\\00\"\n";
        text += "@true_str = private constant [6 x i8] c\"true\\0A\\00\"\n";
        text += "@false_str = private constant [7 x i8] c\"false\\0A\\00\"\n";
        text += "@.bool_fmt = private constant [3 x i8] c\"%s\00\"\n";
        text += "@.str = private unnamed_addr constant [3 x i8] c\"%f\00\"\n";
        text += "@inputBuffer = common global [7 x i8] zeroinitializer\n";
        text += "@boolStr = constant [5 x i8] c\"%6s\\00\\00\"\n";
        text += "@true_str2 = constant [5 x i8] c\"true\\00\"\n";
        text += header_text;
        text += "define i32 @main() nounwind{\n";
        text += main_text;
        text += "ret i32 0 }\n";
        return text;
    }

}