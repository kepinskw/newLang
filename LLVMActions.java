import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum VarType {INT, REAL, FLOAT, STRING, ARRAY_i32, ARRAY_DOUBLE, BOOL, UNKNOWN}

class Value {
    public String name;
    public VarType type;
    public int length;

    public Value(String name, VarType type, int length) {
        this.name = name;
        this.type = type;
        this.length = length;
    }
}


public class LLVMActions extends gramBaseListener {

    HashMap<String, Value> variables = new HashMap<String, Value>();
    Stack<Value> stack = new Stack<Value>();

    static int BUFFER_SIZE = 16;

    @Override
    public void exitProg(gramParser.ProgContext ctx) {
        System.out.println(LLVMGenerator.generate());
    }

    @Override
    public void exitAssign(gramParser.AssignContext ctx) {
        String ID = ctx.ID().getText();
        Value v = stack.pop();
        if (v.type == VarType.INT) {
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_i32(ID);
                variables.put(ID, v);
            }
            LLVMGenerator.assign_i32(ID, v.name);
        }
        if (v.type == VarType.REAL) {
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_double(ID);
                variables.put(ID, v);
            }
            System.err.println("ASSIGN REAK: " + v.name);
            LLVMGenerator.assign_double(ID, v.name);
        }
        if (v.type == VarType.STRING) {
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_string(ID);
                variables.put(ID, v);
            }
            LLVMGenerator.assign_string(ID);
        }
        if (v.type == VarType.BOOL) {
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_bool(ID);
                variables.put(ID, v);
            }
            LLVMGenerator.assign_bool(ID, v.name);
        }if (v.type == VarType.FLOAT) {
            if (!variables.containsKey(ID)) {
                LLVMGenerator.declare_float(ID);
                variables.put(ID, v);
            }
            System.err.println("ASSIGN: " + v.name);
            LLVMGenerator.assign_float(ID, v.name);
        }

    }

    @Override
    public void exitAssignArray(gramParser.AssignArrayContext ctx) {
        String ID = ctx.ID().getText();
        if(ctx.INT() == null){
            error(ctx.getStart().getLine(), "Cannot assign befor initalization");
        }
        String len = ctx.INT().getText();
        System.err.println("len: " + len);
        Value v = stack.pop();
        if (Integer.parseInt(len) == v.length) {
            if (v.type == VarType.ARRAY_i32) {
                System.err.println("name: " + ID);
                System.err.println("len: " + len);
                System.err.println("v: " + v.name);
                if (!variables.containsKey(ID)) {
                    LLVMGenerator.declare_array_i32(ID, v.length);
                }
                String[] content = v.name.substring(1, v.name.length() - 1).split(",");
                List<String> contentList = Arrays.asList(content);
                LLVMGenerator.assign_array_i32(ID, contentList);
                variables.put(ID,v);
            }else {
                System.err.println("name: " + ID);
                System.err.println("len: " + len);
                if (!variables.containsKey(ID)) {
                    LLVMGenerator.declare_array_double(ID, v.length);
                }
                //LLVMGenerator.assign_array_double(ID, v.name);
            }
        } else {
            error(ctx.getStart().getLine(), "declared lenght doesn't match assignment");
        }


    }

    @Override
    public void exitVid(gramParser.VidContext ctx) {
        String ID = ctx.ID().getText();
        if (variables.containsKey(ID)) {
            Value v = variables.get(ID);
            System.err.println("Current push: " + v.name);
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
            if (v1.type == VarType.FLOAT){
                LLVMGenerator.add_float(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
            }
        }else if (v1.type == VarType.FLOAT && v2.type == VarType.REAL) {
            LLVMGenerator.dobtoflo(v2.name);;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.add_float(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        } else if (v1.type == VarType.REAL && v2.type == VarType.FLOAT) {
            LLVMGenerator.flotodob(v2.name);;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.add_double(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
        }
        else if (v1.type == VarType.STRING && v2.type == VarType.INT) {
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
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if (v1.type == VarType.INT) {
            LLVMGenerator.sub_i32(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT, 0));
        } else if (v1.type == VarType.REAL) {
            LLVMGenerator.sub_double(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));

        } else if (v1.type == VarType.FLOAT){
            LLVMGenerator.sub_float(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        }else if (v1.type == VarType.FLOAT && v2.type == VarType.REAL) {
            LLVMGenerator.dobtoflo(v2.name);;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.sub_float(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        } else if (v1.type == VarType.REAL && v2.type == VarType.FLOAT) {
            LLVMGenerator.flotodob(v2.name);;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.sub_double(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
        }
        else {
            error(ctx.getStart().getLine(), "negation type mismatch " + v1.type + " ");
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
            if (v1.type == VarType.FLOAT){
                LLVMGenerator.mult_float(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
            }

        }else if (v1.type == VarType.FLOAT && v2.type == VarType.REAL) {
            LLVMGenerator.dobtoflo(v2.name);;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.mult_float(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        } else if (v1.type == VarType.REAL && v2.type == VarType.FLOAT) {
            LLVMGenerator.flotodob(v2.name);;
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
            if (v1.type == VarType.FLOAT){
                LLVMGenerator.div_double(v2.name, v1.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
            }
        } else if (v1.type == VarType.FLOAT && v2.type == VarType.REAL) {
            LLVMGenerator.dobtoflo(v2.name);;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.div_float(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        } else if (v1.type == VarType.REAL && v2.type == VarType.FLOAT) {
            LLVMGenerator.flotodob(v2.name);;
            v2.name = "%" + (LLVMGenerator.reg - 1);
            LLVMGenerator.div_double(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
        }else {
            error(ctx.getStart().getLine(), "division type mismatch");
        }

    }

    @Override
    public void exitToint(gramParser.TointContext ctx) {
        Value v = stack.pop();
        if (v.type == VarType.REAL){
            LLVMGenerator.fptosi(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT, 0));
        }else if (v.type == VarType.FLOAT){
            LLVMGenerator.float_to_int(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT, 0));

        }else{
            System.err.println("To int type mismatch" + v.type);
        }
    }

    @Override
    public void exitToreal(gramParser.TorealContext ctx) {
        Value v = stack.pop();
        if(v.type == VarType.INT){
            LLVMGenerator.sitofp(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
        } else if (v.type == VarType.FLOAT){
            LLVMGenerator.flotodob(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL, 0));
        }else{
            System.err.println("To real type mismatch" + v.type);
        }
    }

    @Override
    public void exitTofloat(gramParser.TofloatContext ctx) {
        Value v = stack.pop();
        if (v.type == VarType.REAL){
            LLVMGenerator.dobtoflo(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        } else if (v.type == VarType.INT){
            LLVMGenerator.int_to_float(v.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.FLOAT, 0));
        }else{
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
        System.err.println("typ:" + v.type);
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
                LLVMGenerator.printf_arrayi32(ID);
            }
            if (v.type == VarType.FLOAT) {
                LLVMGenerator.printf_float(ID);
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