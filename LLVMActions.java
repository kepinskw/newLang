import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

enum VarType {INT, REAL, UNKNOWN}

class Value {
    public String name;
    public VarType type;

    public Value(String name, VarType type) {
        this.name = name;
        this.type = type;
    }
}


public class LLVMActions extends gramBaseListener {

    //  HashSet<String> variables = new HashSet<String>();
    HashMap<String, VarType> variables = new HashMap<String, VarType>();
    Stack<Value> stack = new Stack<Value>();

    @Override
    public void exitProg(gramParser.ProgContext ctx) {
        System.out.println(LLVMGenerator.generate());
    }

    @Override
    public void exitAssign(gramParser.AssignContext ctx) {
        String ID = ctx.ID().getText();
        Value v = stack.pop();
        variables.put(ID, v.type);
        if (v.type == VarType.INT) {
            LLVMGenerator.declare_i32(ID);
            LLVMGenerator.assign_i32(ID, v.name);
        }
        if (v.type == VarType.REAL) {
            LLVMGenerator.declare_double(ID);
            LLVMGenerator.assign_double(ID, v.name);
        }
    }

    @Override
    public void exitInt(gramParser.IntContext ctx) {
        stack.push(new Value(ctx.INT().getText(), VarType.INT));
    }

    @Override
    public void exitReal(gramParser.RealContext ctx) {
        stack.push(new Value(ctx.REAL().getText(), VarType.REAL));
    }

    @Override
    public void exitAdd(gramParser.AddContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();

        if (v1.type == v2.type) {
            if (v1.type == VarType.INT) {
                LLVMGenerator.add_i32(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT));
            }
            if (v1.type == VarType.REAL) {
                LLVMGenerator.add_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL));
            }
        } else {
            error(ctx.getStart().getLine(), "add type mismatch" + v1.type + " " + v2.type);
        }

    }

    @Override
    public void exitSub(gramParser.SubContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();

        if (v1.type == v2.type) {
            if (v1.type == VarType.INT) {
                LLVMGenerator.sub_i32(v2.name, v1.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT));
            }
            if (v1.type == VarType.REAL) {
                LLVMGenerator.sub_double(v2.name, v1.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL));
            }
        } else {
            error(ctx.getStart().getLine(), "add type mismatch" + v1.type + " " + v2.type);
        }

    }

    @Override
    public void exitMul(gramParser.MulContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();

        if (v1.type == v2.type) {
            if (v1.type == VarType.INT) {
                LLVMGenerator.mult_i32(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT));
            }
            if (v1.type == VarType.REAL) {
                LLVMGenerator.mult_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL));
            }
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
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT));
            }
            if (v1.type == VarType.REAL) {
                LLVMGenerator.div_double(v2.name, v1.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL));
            }
        } else {
            error(ctx.getStart().getLine(), "division type mismatch");
        }

    }

    @Override
    public void exitToint(gramParser.TointContext ctx) {
        Value v = stack.pop();
        LLVMGenerator.fptosi(v.name);
        stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT));
    }

    @Override
    public void exitToreal(gramParser.TorealContext ctx) {
        Value v = stack.pop();
        LLVMGenerator.sitofp(v.name);
        stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL));
    }

    @Override
    public void exitPrint(gramParser.PrintContext ctx) {
        String ID = ctx.ID().getText();
        VarType type = variables.get(ID);
        if (type != null) {
            if (type == VarType.INT) {
                LLVMGenerator.printf_i32(ID);
            }
            if (type == VarType.REAL) {
                LLVMGenerator.printf_double(ID);
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
        VarType type = variables.get(ID);
        if (type == VarType.INT) {
            LLVMGenerator.scanf_i32(ID); // Wywołuje funkcję scanf dla int

        } else if (type == VarType.REAL) {
            LLVMGenerator.scanf_double(ID); // Wywołuje funkcję scanf_double dla double
        }

    }

    void error(int line, String msg) {
        System.err.println("Error, line " + line + ", " + msg);
        System.exit(1);
    }


}