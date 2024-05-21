import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum VarType {INT, REAL, FLOAT, STRING, ARRAY_i32, ARRAY_i32_ELEM, MATRIX_DOUBLE, MATRIX_I32, ARRAY_DOUBLE, BOOL, UNKNOWN}

class Value {
    public String name;
    public VarType type;
    public int length;
    public int length1;

    public Value(String name, VarType type, int length, int length1) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.length1 = length1;
    }

    public Value(String name, VarType type, int length) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.length1 = 0;
    }
}


public class LLVMActions extends gramBaseListener {

    HashMap<String, Value> variables = new HashMap<String, Value>();
    Stack<Value> stack = new Stack<Value>();
    Boolean global = false;

    static int BUFFER_SIZE = 16;

    @Override
    public void exitProg(gramParser.ProgContext ctx) {
        System.out.println(LLVMGenerator.generate());
    }

    @Override
    public void exitAssign(gramParser.AssignContext ctx) {
        String ID = ctx.ID().getText();
        Value v = stack.pop();
        System.err.println("v.nameee  " + v.type);
        if (v.type == VarType.INT) {
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_i32(ID);
                variables.put(ID, v);
            } else if (variables.get(ID).type != VarType.INT) {
                error(ctx.getStart().getLine(), "Type missmach");
            }
            LLVMGenerator.assign_i32(ID, v.name);
        } else if (v.type == VarType.REAL) {
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_double(ID);
                variables.put(ID, v);
            } else if (variables.get(ID).type != VarType.REAL) {
                error(ctx.getStart().getLine(), "Type missmach");
            }
            LLVMGenerator.assign_double(ID, v.name);
        } else if (v.type == VarType.STRING) {
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_string(ID);
                variables.put(ID, v);
            } else if (variables.get(ID).type != VarType.STRING) {
                error(ctx.getStart().getLine(), "Type missmach");
            }
            LLVMGenerator.assign_string(ID);
        } else if (v.type == VarType.BOOL) {
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_bool(ID);
                variables.put(ID, v);
            } else if (variables.get(ID).type != VarType.BOOL) {
                error(ctx.getStart().getLine(), "Type missmach");
            }
            LLVMGenerator.assign_bool(ID, v.name);
        }
        if (v.type == VarType.FLOAT) {
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_float(ID);
                variables.put(ID, v);
            }
            System.err.println("ASSIGN: " + v.name);
            LLVMGenerator.assign_float(ID, v.name);
        } else if (v.type == VarType.ARRAY_i32) {
            //error(ctx.getStart().getLine(), "assign TPYE ARRAY_I32");
            Value realValue = stack.pop();
            Value array = variables.get(v.name);
            //error(ctx.getStart().getLine(), "assign TPYE ARRAY_I32" + array.txt.type);
            System.err.println("v.nameee  " + v.name);
            System.err.println("arrayyy.nameee  " + array.type);

            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_i32(ID);
                variables.put(ID, new Value(realValue.name, VarType.INT, 1));

            } else if (v.type != VarType.INT) {
                error(ctx.getStart().getLine(), "Type missmach");
            }
            System.err.println("Assigning  " + v.name);
            LLVMGenerator.assign_i32_from_array(v.name, ID, array.length, Integer.parseInt(realValue.name));

        } else if (v.type == VarType.ARRAY_DOUBLE) {
            Value realValue = stack.pop();
            Value array = variables.get(v.name);
            System.err.println("v.nameee  " + v.name);
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_double(ID);
                variables.put(ID, new Value(realValue.name, VarType.REAL, 1));
            } else if (v.type != VarType.REAL) {
                error(ctx.getStart().getLine(), "Type missmach");
            }
            LLVMGenerator.assign_double_from_array(v.name, ID, array.length, Integer.parseInt(realValue.name));

        } else if (v.type == VarType.MATRIX_I32) {
            Value column = stack.pop();
            Value row = stack.pop();
            System.err.println("v.nameee  Hereee" + v.name);
            if(column.type == VarType.INT && row.type == VarType.INT) {
                if (!variables.containsKey(ID)) {
                    LLVMGenerator.declare_i32(ID);
                    variables.put(ID, new Value(v.name, VarType.INT, 1));
                } else if (v.type != VarType.REAL) {
                    error(ctx.getStart().getLine(), "Type missmach");
                }
                LLVMGenerator.assign_i32_from_matrix(v.name, ID, v.length, v.length1, Integer.parseInt(row.name), Integer.parseInt(column.name));
                //error(ctx.getStart().getLine(), "v.name123" + v.name);

            }else {
                System.err.println("array form matrix   " + column.type);
                System.err.println("array form matrix   ID " + row.type);
                if(column.type == VarType.INT && row.type == VarType.STRING) {
                    System.err.println("array assign " + ID);
                    if (!variables.containsKey(ID)) {
                        System.err.println("declared len" + v.length1);
                        LLVMGenerator.declare_array_i32(ID, v.length1);
                        variables.put(ID, new Value(v.name, VarType.ARRAY_i32, v.length1));

                    } else if (v.type != VarType.ARRAY_i32) {
                        error(ctx.getStart().getLine(), "Type missmach");
                    }
                    System.err.println("assign row len" + v.length);
                    System.err.println("assign col len" + v.length1);
                    LLVMGenerator.assign_array_i32_from_matrix(v.name, ID, v.length, v.length1, Integer.parseInt(column.name));
                }else if(column.type == VarType.STRING && row.type == VarType.INT) {
                    System.err.println("column assign " + v.length);
                    if (!variables.containsKey(ID)) {
                        System.err.println("declare with len " + v.length);
                        LLVMGenerator.declare_array_i32(ID, v.length);
                        variables.put(ID, new Value(v.name, VarType.ARRAY_i32, v.length));

                    } else if (v.type != VarType.ARRAY_i32) {
                        error(ctx.getStart().getLine(), "Type missmach");
                    }
                    LLVMGenerator.assign_column_i32_from_matrix(v.name, ID, v.length, v.length1, Integer.parseInt(row.name));
                    System.err.println("done " + v.length);
                    System.err.println("done " + v.length1);
                }
            }

        }

    }

    @Override
    public void exitMatrixColumn(gramParser.MatrixColumnContext ctx) {
        String ID = ctx.ID().getText();
        String columnID = ctx.INT().getText();
        //Integer INT = Integer.parseInt(textInt);
        Value matrix = variables.get(ID);
        //error(ctx.getStart().getLine(), "matrix name: " + matrix);
        if (Integer.parseInt(columnID) < matrix.length1) {
            stack.push(new Value(columnID, VarType.INT, 1));
            stack.push(new Value("all", VarType.STRING, 1));
            stack.push(new Value(ID, matrix.type, matrix.length, matrix.length1));
        } else {
            error(ctx.getStart().getLine(), "Referenced outside matrix");
        }
    }

    @Override
    public void exitAssignArrayElem(gramParser.AssignArrayElemContext ctx) {
        String ID = ctx.ID().getText();
        String INT = ctx.INT().getText();
        System.err.println("EXIT ARRAY ELEMENT   : " + ID);
        if (!variables.containsKey(ID)) {
            error(ctx.getStart().getLine(), "Vector " + ID + "does not exist");
        }
        Value array = variables.get(ID);
        if (Integer.parseInt(INT) >= array.length) {
            error(ctx.getStart().getLine(), "Referenced outside vector");
        }
        Value value = stack.pop();
        //error(ctx.getStart().getLine(), "Referenced outside vector" + value.type);
        if (value.type == VarType.INT) {
            LLVMGenerator.assign_array_i32_element(ID, Integer.parseInt(value.name), array.length, Integer.parseInt(INT));
        } else {
            LLVMGenerator.assign_array_double_element(ID, value.name, array.length, Integer.parseInt(INT));
        }

    }

    @Override
    public void exitAssignMatrixElem(gramParser.AssignMatrixElemContext ctx) {
        String ID = ctx.ID().getText();
        String row = ctx.INT(0).getText();
        String col = ctx.INT(1).getText();
        System.err.println("EXIT ARRAY ELEMENT   : " + ID);
        if (!variables.containsKey(ID)) {
            error(ctx.getStart().getLine(), "Vector " + ID + "does not exist");
        }
        Value matrix = variables.get(ID);
        if (Integer.parseInt(row) >= matrix.length | Integer.parseInt(col) >= matrix.length1) {
            error(ctx.getStart().getLine(), "Referenced outside vector");
        }
        Value value = stack.pop();
        LLVMGenerator.assign_matrix_i32_element(ID, Integer.parseInt(value.name), matrix.length, matrix.length1, Integer.parseInt(row), Integer.parseInt(col));
    }

    @Override
    public void exitAssignArray(gramParser.AssignArrayContext ctx) {
        String ID = ctx.ID().getText();
        Integer len = 0;
        if (ctx.INT() == null) {
            if (variables.containsKey(ID)) {
                Value dest = variables.get(ID);
                len = dest.length;
            } else {
                error(ctx.getStart().getLine(), "Cannot assign before initalization");
            }

        } else {
            len = Integer.parseInt(ctx.INT().getText());
        }
        System.err.println("len: " + len);
        Value v = stack.pop();
        if (len == v.length) {
            if (v.type == VarType.ARRAY_i32) {
                System.err.println("name: " + ID);
                System.err.println("len: " + len);
                System.err.println("v: " + v.name);
                if (!variables.containsKey(ID)) {
                    LLVMGenerator.declare_array_i32(ID, v.length);
                    System.err.println("array.txt declaration   : " + ID);

                }
                String[] content = v.name.substring(1, v.name.length() - 1).split(",");
                List<String> contentList = Arrays.asList(content);
                LLVMGenerator.assign_array_i32(ID, contentList);
                System.err.println("VARIABLE exitAssignArray: " + ID);
                variables.put(ID, v);
            } else {
                System.err.println("name: " + ID);
                System.err.println("len: " + len);
                System.err.println("v: " + v.name);
                if (!variables.containsKey(ID)) {
                    LLVMGenerator.declare_array_double(ID, v.length);
                }
                String[] content = v.name.substring(1, v.name.length() - 1).split(",");
                List<String> contentList = Arrays.asList(content);
                LLVMGenerator.assign_array_double(ID, contentList);
                System.err.println("VARIABLE exitAssignArray: " + ID);
                variables.put(ID, v);
            }
        } else {
            error(ctx.getStart().getLine(), "declared lenght doesn't match assignment");
        }


    }

    @Override
    public void exitAssignMatrix(gramParser.AssignMatrixContext ctx) {
        String ID = ctx.ID().getText();
        int rowsLen = 0;
        int columnLen = 0;
        if (ctx.INT(0) == null) {
            if (variables.containsKey(ID)) {
                Value dest = variables.get(ID);
                rowsLen = dest.length;
                columnLen = dest.length1;
            } else {
                error(ctx.getStart().getLine(), "Cannot assign before initalization");
            }
        } else {
            String rows = ctx.INT(0).getText();
            String columns = ctx.INT(1).getText();
            rowsLen = Integer.parseInt(rows);
            columnLen = Integer.parseInt(columns);

        }
        Value matrix = stack.pop();
        //error(ctx.getStart().getLine(), " assign matrix " + matrix.name);
        if (rowsLen == matrix.length & columnLen == matrix.length1) {
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_matrix_i32(ID, matrix.length, matrix.length1);
            }
            List<String> elements = Arrays.asList(matrix.name.substring(1, matrix.name.length() - 1).split("[,;]"));
            //error(ctx.getStart().getLine(), "matrix: " + matrix.name);
            LLVMGenerator.assign_matrix_i32(ID, rowsLen, columnLen, elements);
            variables.put(ID, matrix);
        } else {
            error(ctx.getStart().getLine(), "missmatch size");
        }

    }

    @Override
    public void exitArrayLetter(gramParser.ArrayLetterContext ctx) {
        String ID = ctx.ID().getText();
        String textInt = ctx.INT().getText();
        //Integer INT = Integer.parseInt(textInt);
        Value array = variables.get(ID);
        if (Integer.parseInt(textInt) < array.length) {
            if (array.type == VarType.ARRAY_i32 || array.type == VarType.ARRAY_DOUBLE) {
                stack.push(new Value(textInt, VarType.INT, 1));
                stack.push(new Value(ID, array.type, array.length));
            } else {
                stack.push(new Value("all", VarType.STRING, 1));
                stack.push(new Value(textInt, VarType.INT, 1));
                stack.push(new Value(ID, VarType.MATRIX_I32, array.length,array.length1));
                System.err.println("Matrix to array start: " + ID);
            }
        } else {
            error(ctx.getStart().getLine(), "Referenced outside vector");
        }


    }

    @Override
    public void exitMatrixLetter(gramParser.MatrixLetterContext ctx) {
        String ID = ctx.ID().getText();
        String rowID = ctx.INT(0).getText();
        String columnID = ctx.INT(1).getText();
        //Integer INT = Integer.parseInt(textInt);
        Value matrix = variables.get(ID);
        //error(ctx.getStart().getLine(), "matrix name: " + matrix);
        if (Integer.parseInt(rowID) < matrix.length & Integer.parseInt(columnID) < matrix.length1) {
            stack.push(new Value(rowID, VarType.INT, 1));
            stack.push(new Value(columnID, VarType.INT, 1));
            stack.push(new Value(ID, matrix.type, matrix.length, matrix.length1));
        } else {
            error(ctx.getStart().getLine(), "Referenced outside matrix");
        }
    }

    @Override
    public void exitVid(gramParser.VidContext ctx) {
        String ID = ctx.ID().getText();
        if (variables.containsKey(ID)) {
            Value v = variables.get(ID);
            System.err.println("Current push: " + ID);
            if (v.type == VarType.INT) {
                LLVMGenerator.load_i32(ID);
            }
            if (v.type == VarType.REAL) {
                LLVMGenerator.load_double(ID);
            }
            if (v.type == VarType.STRING) {
                LLVMGenerator.load_string(ID);
            }
            if (v.type == VarType.FLOAT) {
                LLVMGenerator.load_float(ID);
            }
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), v.type, v.length));
        } else {
            error(ctx.getStart().getLine(), "unknown variable " + ID);
        }
    }

    @Override
    public void exitInt(gramParser.IntContext ctx) {
        stack.push(new Value(ctx.INT().getText(), VarType.INT, 0));
    }

    @Override
    public void exitReal(gramParser.RealContext ctx) {
        stack.push(new Value(ctx.REAL().getText(), VarType.REAL, 0));
    }

    @Override
    public void exitString(gramParser.StringContext ctx) {
        String tmp = ctx.STRING().getText();
        String content = tmp.substring(1, tmp.length() - 1);
        LLVMGenerator.constant_string(content);
        String n = "ptrstr" + (LLVMGenerator.str - 1);
        stack.push(new Value(n, VarType.STRING, content.length()));
    }

    @Override
    public void exitIntArray(gramParser.IntArrayContext ctx) {
        String tmp = ctx.getText();
        String[] content = tmp.substring(1, tmp.length() - 1).split(",");
        System.err.println(tmp);
        Value v = new Value(tmp, VarType.ARRAY_i32, content.length);
        stack.push(v);
    }

    @Override
    public void exitRealArray(gramParser.RealArrayContext ctx) {
        String tmp = ctx.getText();
        String[] content = tmp.substring(1, tmp.length() - 1).split(",");
        System.err.println(tmp);
        Value v = new Value(tmp, VarType.ARRAY_DOUBLE, content.length);
        stack.push(v);
    }

    @Override
    public void exitIntMatrix(gramParser.IntMatrixContext ctx) {
        String tmp = ctx.getText();
        System.err.println(tmp);
        String[] rows = tmp.substring(1, tmp.length() - 1).split(";");
        System.err.println(rows.length);
        String[] array = rows[0].split(",");
        System.err.println(array.length);
        System.err.println(tmp);
        Value v = new Value(tmp, VarType.MATRIX_I32, rows.length, array.length);
        stack.push(v);
    }

    @Override
    public void exitAdd(gramParser.AddContext ctx) {
        if (stack.isEmpty()) {
            error(ctx.getStart().getLine(), "stack empty");
        }
        Value v2 = stack.pop();
        Value v1 = stack.pop();
        System.err.println("typ:" + v2.type);
        System.err.println("typ:" + v1.type);

        if (v1.type == v2.type) {
            if (v1.type == VarType.INT) {
                LLVMGenerator.add_i32(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT, 0));
            }
            if (v1.type == VarType.REAL) {
                LLVMGenerator.add_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
            }
            if (v1.type == VarType.STRING) {
                LLVMGenerator.add_string(v1.name, v1.length, v2.name, v2.length);
                stack.push(new Value("%" + (LLVMGenerator.reg - 3), VarType.STRING, v1.length));
            }
            if (v1.type == VarType.FLOAT) {
                LLVMGenerator.add_float(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
            }
        } else if (v1.type == VarType.FLOAT && v2.type == VarType.REAL) {
            LLVMGenerator.dobtoflo(v2.name);
            ;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.add_float(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        } else if (v1.type == VarType.REAL && v2.type == VarType.FLOAT) {
            LLVMGenerator.flotodob(v2.name);
            ;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.add_double(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
        } else if (v1.type == VarType.STRING && v2.type == VarType.INT) {
            LLVMGenerator.int_to_string(v2.name, BUFFER_SIZE);
            v2.name = "%" + (LLVMGenerator.reg - 2);
            LLVMGenerator.add_string(v1.name, v1.length, v2.name, BUFFER_SIZE);
            stack.push(new Value("%" + (LLVMGenerator.reg - 3), VarType.STRING, v1.length));
        } else if (v1.type == VarType.INT && v2.type == VarType.STRING) {
            LLVMGenerator.string_to_int(v2.name);
            LLVMGenerator.add_i32(v1.name, "%" + (LLVMGenerator.reg - 1));
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT, 0));
        } else {
            error(ctx.getStart().getLine(), "add type mismatch " + v1.type + " " + v2.type);
        }

    }

    @Override
    public void exitSub(gramParser.SubContext ctx) {
        Value v2 = stack.pop();
        Value v1 = stack.pop();
        if (v1.type == v2.type) {
            if (v1.type == VarType.INT) {
                LLVMGenerator.sub_i32(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT, 0));
            } else if (v1.type == VarType.REAL) {
                LLVMGenerator.sub_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));

            } else if (v1.type == VarType.FLOAT) {
                LLVMGenerator.sub_float(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
            } else {
                error(ctx.getStart().getLine(), "negation type mismatch " + v1.type + " ");
            }
        } else {
            if (v1.type == VarType.FLOAT && v2.type == VarType.REAL) {
                LLVMGenerator.dobtoflo(v2.name);
                ;
                v2.name = "%" + (LLVMGenerator.reg - 1);
                LLVMGenerator.sub_float(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
            } else if (v1.type == VarType.REAL && v2.type == VarType.FLOAT) {
                LLVMGenerator.flotodob(v2.name);
                ;
                v2.name = "%" + (LLVMGenerator.reg - 1);
                LLVMGenerator.sub_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
            } else {
                error(ctx.getStart().getLine(), "negation type mismatch " + v1.type + " ");
            }
        }
    }

    @Override
    public void exitMul(gramParser.MulContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();

        if (v1.type == v2.type) {
            if (v1.type == VarType.INT) {
                LLVMGenerator.mult_i32(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT, 0));
            }
            if (v1.type == VarType.REAL) {
                LLVMGenerator.mult_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
            }
            if (v1.type == VarType.FLOAT) {
                LLVMGenerator.mult_float(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
            }

        } else if (v1.type == VarType.FLOAT && v2.type == VarType.REAL) {
            LLVMGenerator.dobtoflo(v2.name);
            ;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.mult_float(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        } else if (v1.type == VarType.REAL && v2.type == VarType.FLOAT) {
            LLVMGenerator.flotodob(v2.name);
            ;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.mult_double(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
        } else {
            error(ctx.getStart().getLine(), "multiplication type mismatch");
        }

    }

    @Override
    public void exitDivide(gramParser.DivideContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();

        if (v1.type == v2.type) {
            if (v1.type == VarType.INT) {
                LLVMGenerator.div_i32(v2.name, v1.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT, 0));
            }
            if (v1.type == VarType.REAL) {
                LLVMGenerator.div_double(v2.name, v1.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
            }
            if (v1.type == VarType.FLOAT) {
                LLVMGenerator.div_float(v2.name, v1.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
            }
        } else if (v1.type == VarType.FLOAT && v2.type == VarType.REAL) {
            LLVMGenerator.dobtoflo(v2.name);
            ;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.div_float(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        } else if (v1.type == VarType.REAL && v2.type == VarType.FLOAT) {
            LLVMGenerator.flotodob(v2.name);
            ;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.div_double(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
        } else {
            error(ctx.getStart().getLine(), "division type mismatch");
        }

    }

    @Override
    public void exitToint(gramParser.TointContext ctx) {
        Value v = stack.pop();
        if (v.type == VarType.REAL) {
            LLVMGenerator.fptosi(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT, 0));
        } else if (v.type == VarType.FLOAT) {
            LLVMGenerator.float_to_int(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT, 0));

        } else {
            System.err.println("To int type mismatch" + v.type);
        }
    }

    @Override
    public void exitToreal(gramParser.TorealContext ctx) {
        Value v = stack.pop();
        if (v.type == VarType.INT) {
            LLVMGenerator.sitofp(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
        } else if (v.type == VarType.FLOAT) {
            LLVMGenerator.flotodob(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
        } else {
            System.err.println("To real type mismatch" + v.type);
        }
    }

    @Override
    public void exitTofloat(gramParser.TofloatContext ctx) {
        Value v = stack.pop();
        if (v.type == VarType.REAL) {
            LLVMGenerator.dobtoflo(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        } else if (v.type == VarType.INT) {
            LLVMGenerator.int_to_float(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        } else {
            System.err.println("To float type mismatch" + v.type);
        }

    }

    @Override
    public void exitBool(gramParser.BoolContext ctx) {
        System.err.println("Current push: " + ctx.BOOL().getText());
        stack.push(new Value(ctx.BOOL().getText(), VarType.BOOL, 0));
    }

    @Override
    public void exitAnd(gramParser.AndContext ctx) {
        System.err.println("AND: ");

        Value v2 = stack.pop();
        Value v1 = stack.pop();

        LLVMGenerator.log_and(v1.name, v2.name);
        stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.BOOL, 0));
    }

    @Override
    public void exitOr(gramParser.OrContext ctx) {
        System.err.println("OR: ");
        Value v2 = stack.pop();
        Value v1 = stack.pop();

        LLVMGenerator.log_or(v1.name, v2.name);
        stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.BOOL, 0));
    }

    @Override
    public void exitXor(gramParser.XorContext ctx) {
        Value v2 = stack.pop();
        Value v1 = stack.pop();

        LLVMGenerator.log_xor(v1.name, v2.name);
        stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.BOOL, 0));
    }

    @Override
    public void exitNeg(gramParser.NegContext ctx) {
        Value v1 = stack.pop();

        LLVMGenerator.log_neg(v1.name);
        stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.BOOL, 0));
    }

    @Override
    public void exitPrint(gramParser.PrintContext ctx) {
        String ID = ctx.ID().getText();
        Value v = variables.get(ID);
        System.err.println("Error, line IDD print  " + ID);
        if (v.type != null) {
            if (v.type == VarType.INT) {
                LLVMGenerator.printf_i32(ID);
            }
            if (v.type == VarType.REAL) {
                LLVMGenerator.printf_double(ID);
            }
            if (v.type == VarType.STRING) {
                LLVMGenerator.printf_string(ID);
            }
            if (v.type == VarType.BOOL) {
                LLVMGenerator.printf_bool(ID);
            }
            if (v.type == VarType.ARRAY_i32) {
                System.err.println("Error, printf_array_i32  " + v.length);
                LLVMGenerator.printf_array_i32(ID, v.length);
            }
            if (v.type == VarType.ARRAY_DOUBLE) {
                LLVMGenerator.printf_array_double(ID, v.length);
            }
            if (v.type == VarType.MATRIX_I32) {
                LLVMGenerator.printf_matrix_i32(ID, v.length, v.length1);
            }
            if (v.type == VarType.FLOAT) {
                LLVMGenerator.printf_float(ID);
            }
        } else {
            error(ctx.getStart().getLine(), "unknown variable type " + ID);
        }
    }

    @Override
    public void enterBlockif(gramParser.BlockifContext ctx) {
        LLVMGenerator.ifstart();
    }

    @Override
    public void exitBlockif(gramParser.BlockifContext ctx) {
        LLVMGenerator.ifend();
    }

    @Override
    public void exitCond(gramParser.CondContext ctx) {
        String ID = ctx.ID().getText();
        String INT = ctx.INT().getText();
        if (variables.containsKey(ID)) {
            LLVMGenerator.icmp(ID, INT);
        } else {
            error(ctx.getStart().getLine(), "uknown variable: " + ID);
        }
    }

    @Override
    public void exitReps(gramParser.RepsContext ctx) {
        if (ctx.ID() != null) {
            String ID = ctx.ID().getText();
            if (variables.containsKey(ID)) {
                LLVMGenerator.load_i32(ID);
                LLVMGenerator.startloop("%" + (LLVMGenerator.reg - 1));
            } else {
                error(ctx.getStart().getLine(), "uknown variable: " + ID);
            }
        } else {
            String INT = ctx.INT().getText();
            LLVMGenerator.startloop(INT);
        }
    }

    @Override
    public void exitBlock(gramParser.BlockContext ctx) {
        if (ctx.getParent() instanceof gramParser.ForContext) {
            LLVMGenerator.endloop();
        }
    }

    @Override
    public void exitPrintLetter(gramParser.PrintLetterContext ctx) {
        String IDstring = ctx.letter().getText();
        String ID = IDstring.replaceAll("\\[\\d*\\]", "");
        System.err.println("Variable IDDDD " + ID);
        Value v = variables.get(ID);
        System.err.println("Variable " + v.type);
        if (v.type != null) {
            if (v.type == VarType.ARRAY_i32) {
                Value elementId = stack.pop();
                Value position = stack.pop();
                System.err.println("Variable " + position.name);
                LLVMGenerator.printf_array_i32_element(elementId.name, v.length, Integer.parseInt(position.name));
            } else if (v.type == VarType.ARRAY_DOUBLE) {
                Value elementId = stack.pop();
                Value position = stack.pop();
                System.err.println("Variable " + position.name);
                LLVMGenerator.printf_array_double_element(elementId.name, v.length, Integer.parseInt(position.name));
            } else if (v.type == VarType.MATRIX_I32) {
                Value elementId = stack.pop();
                Value columnsId = stack.pop();
                System.err.println("Element ID name " + elementId.name);
                if (!stack.isEmpty()) {
                    System.err.println("noemptystack " + elementId.name);
                    Value rowId = stack.pop();
                    System.err.println("noemptystack row " + rowId.type);
                    System.err.println("noemptystack column " + columnsId.type);
                    if (columnsId.type == VarType.INT && rowId.type == VarType.INT) {
                        LLVMGenerator.printf_matrix_i32_element(elementId.name, v.length, v.length1, Integer.parseInt(rowId.name), Integer.parseInt(columnsId.name));
                    } else if (columnsId.type == VarType.STRING && rowId.type == VarType.INT) {
                        LLVMGenerator.printf_column_i32_from_matrix(elementId.name, v.length, v.length1, Integer.parseInt(rowId.name));
                        System.err.println("column " + elementId.name);
                    } else if (columnsId.type == VarType.INT && rowId.type == VarType.STRING) {
                        System.err.println("array " + elementId.name);
                        LLVMGenerator.printf_array_i32_from_matrix(elementId.name, v.length, v.length1, Integer.parseInt(columnsId.name));
                    }
                }
            }
        } else {
            error(ctx.getStart().getLine(), "unknown variable type" + ID);
        }
    }

    @Override
    public void exitRead(gramParser.ReadContext ctx) {
        String ID = ctx.ID().getText();
        if (!variables.containsKey(ID)) {
            error(ctx.getStart().getLine(), ID + "undeclared");
        }
        Value v = variables.get(ID);
        if (v.type == VarType.INT) {
            LLVMGenerator.scanf_i32(ID);
        } else if (v.type == VarType.REAL) {
            LLVMGenerator.scanf_double(ID, v.name);
        } else if (v.type == VarType.STRING) {
            LLVMGenerator.scanf_string(ID, BUFFER_SIZE);
        } else if (v.type == VarType.BOOL) {
            LLVMGenerator.scanf_bool(ID);
        }

    }

    void error(int line, String msg) {
        System.err.println("Error, line " + line + ", " + msg);
        System.exit(1);
    }


}