import org.stringtemplate.v4.ST;

import java.util.List;

class LLVMGenerator {

    static String header_text = "";
    static String main_text = "";
    static int reg = 1;
    static int str = 1;

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

    static void printf_string(String id) {
        main_text += "%" + reg + " = load i8*, i8** %" + id + "\n";
        reg++;
        main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strps, i32 0, i32 0), i8* %" + (reg - 1) + ")\n";
        reg++;
    }

    static void printf_arrayi32(String id) {
      main_text += "%" + reg + " = load i32*, i32** %" + id + "\n";
      reg++;
      main_text += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strps, i32 0, i32 0), i32* %" + (reg - 1) + ")\n";
      reg++;
  }

    static void scanf_i32(String id) {
        main_text += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i32* %" + id + ")\n";
        reg++;
    }

    static void scanf_bool(String id) {
        main_text += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i1* %" + id + ")\n";
        reg++;
    }

    static void allocate_string(String id, int l) {
        main_text += "%" + id + " = alloca [" + (l + 1) + " x i8]\n";
    }

    static void scanf_double(String id, String val) {
        main_text += "%" + reg + " = alloca double\n";
        int allocaReg = reg;
        reg++;
        main_text += "store double "+ val + ", double* %" + id+ "\n";
        main_text += "%"+ reg +" = call i32 (i8*, ...) @__isoc99_scanf(i8* noundef getelementptr inbounds ([4 x i8], [4 x i8]* @.strDouble, i64 0, i64 0), double*  %" + allocaReg + ")\n";
        reg++;
        main_text += "%" + reg + " = load double, double* %" + allocaReg + "\n";
        reg++;
        main_text += "store double %"+ (reg-1) + ", double* %" + id + "\n";
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

    static void declare_array_i32(String id, int size) {
        main_text += "%" + id + " = alloca [" + (size+1) + " x i32]\n";
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

    static void assign_array_i32(String id, List<String> values) {

      //   for (int i = 0; i < values.size(); i++) {
      //       // Obliczanie adresu elementu wektora
      //       String elementPtr = "%" + id + "_elem_" + i;
      //       main_text += elementPtr + " = getelementptr inbounds [" + values.size() + " x i32], [" + values.size() + " x i32]* %" + id + ", i32 0, i32 " + i + "\n";
      //       // Generowanie kodu do przypisania wartości do odpowiednich indeksów wektora
      //       main_text += "store i32 " + values.get(i) + ", i32* " + elementPtr + "\n";
      //   }
      main_text += "store i32* %" + (reg - 1) + ", i32** %" + id + "\n";

    }

    static void assign_bool(String id, String value) {
        main_text += "store i1 " + value + ", i1* %" + id + "\n";
    }

    static void assign_double(String id, String value) {
        main_text += "store double " + value + ", double* %" + id + "\n";
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

    static void load_string(String id) {
        main_text += "%" + reg + " = load i8*, i8** %" + id + "\n";
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

    static void sub_i32(String val2, String val1) {
        main_text += "%" + reg + " = sub i32 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void sub_double(String val1, String val2) {
        main_text += "%" + reg + " = fsub double " + val1 + ", " + val2 + "\n";
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

    static void div_i32(String val1, String val2) {
        main_text += "%" + reg + " = sdiv i32 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    static void div_double(String val1, String val2) {
        main_text += "%" + reg + " = fdiv double " + val1 + ", " + val2 + "\n";
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
      main_text += "%" + reg + " = call i16 @llvm.convert.to.fp16.f64(double %"+ id +")\n";
      main_text += "store i16 %"+ reg +", i16* @x";
      reg++;
    }

    static void flotodob(String id) {
      main_text += "%" + reg + " = fptosi double " + id + " to i32\n";
      reg++;
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

    static String generate() {
        String text = "";
        text += "declare i32 @printf(i8*, ...)\n";
        text += "declare i16 @llvm.convert.to.fp16.f64(double %a)\n";
        text += "declare i32 @sprintf(i8*, i8*, ...)\n";
        text += "declare i8* @strcpy(i8*, i8*)\n";
        text += "declare i8* @strcat(i8*, i8*)\n";
        text += "declare i32 @atoi(i8*)\n";
        text += "declare i32 @__isoc99_scanf(i8*, ...)\n";
        text += "declare double @double_scanf(i8*, ...)\n";
        text += "declare void @llvm.memcpy.p0i8.p0i8.i64(i8* noalias nocapture writeonly, i8* noalias nocapture readonly, i64, i1 immarg)\n";
        text += "@strpi = constant [4 x i8] c\"%d\\0A\\00\"\n";
        text += "@strpd = constant [4 x i8] c\"%f\\0A\\00\"\n";
        text += "@strs = constant [3 x i8] c\"%d\\00\"\n";
        text += "@strs2 = constant [5 x i8] c\"%10s\\00\"\n";
        text += "@strspi = constant [3 x i8] c\"%d\\00\"\n";
        text += "@.strDouble = private constant [4 x i8] c\"%lf\00\"\n";
        text += "@strps = constant [4 x i8] c\"%s\\0A\\00\"\n";
        text += "@.bool_str = private constant [3 x i8] c\"%d\00\"\n";
        text += "@.false_str = private constant [3 x i8] c\"%d\00\"\n";
        text += "@true_str = private constant [6 x i8] c\"true\\0A\\00\"\n";
        text += "@false_str = private constant [7 x i8] c\"false\\0A\\00\"\n";
        text += "@.bool_fmt = private constant [3 x i8] c\"%s\00\"\n";
        text += "@.str = private unnamed_addr constant [3 x i8] c\"%f\00\"\n";
        text += header_text;
        text += "define i32 @main() nounwind{\n";
        text += main_text;
        text += "ret i32 0 }\n";
        return text;
    }

}