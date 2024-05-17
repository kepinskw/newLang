import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

enum VarType {INT, REAL, STRING, ARRAY,UNKNOWN}

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
            if(!variables.containsKey(ID)) {
                LLVMGenerator.declare_i32(ID);
                variables.put(ID, v);
            }
            LLVMGenerator.assign_i32(ID, v.name);
        }
        if (v.type == VarType.REAL) {
            if(!variables.containsKey(ID)) {
                LLVMGenerator.declare_double(ID);
                variables.put(ID, v);
            }
            LLVMGenerator.assign_double(ID, v.name);
        }
        if (v.type == VarType.STRING){
            if(!variables.containsKey(ID)) {
                LLVMGenerator.declare_string(ID);
                variables.put(ID, v);
            }
            LLVMGenerator.assign_string(ID);
        }
    }

    @Override
    public void exitVid(gramParser.VidContext ctx){
        String ID = ctx.ID().getText();
        if (variables.containsKey(ID)){
            Value v = variables.get(ID);
            if (v.type == VarType.INT){
               LLVMGenerator.load_i32(ID);
            }
            if (v.type == VarType.REAL){
               LLVMGenerator.load_double(ID);
            }
            if (v.type == VarType.STRING){
               LLVMGenerator.load_string(ID);
            }
            stack.push(new Value("%"+(LLVMGenerator.reg - 1),v.type, v.length));
        } else {
         error(ctx.getStart().getLine(), "unknown variable "+ID);         
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
    public void exitString(gramParser.StringContext ctx){
        String tmp = ctx.STRING().getText();
        String content = tmp.substring(1, tmp.length()-1);
        LLVMGenerator.constant_string(content);
        String n = "ptrstr"+(LLVMGenerator.str-1);
        stack.push(new Value(n, VarType.STRING, content.length()));
    }

    @Override
    public void exitAdd(gramParser.AddContext ctx) {
        if (stack.isEmpty()){
         error(ctx.getStart().getLine(), "stack empty");
      }      
        Value v2 = stack.pop();
        Value v1 = stack.pop();

        if (v1.type == v2.type) {
            if (v1.type == VarType.INT) {
                LLVMGenerator.add_i32(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT,0));
            }
            if (v1.type == VarType.REAL) {
                LLVMGenerator.add_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL,0));
            }
            if (v1.type == VarType.STRING){
                LLVMGenerator.add_string(v1.name, v1.length, v2.name, v2.length);
                stack.push(new Value("%" + (LLVMGenerator.reg - 3), VarType.STRING, v1.length));
            }
        } else if (v1.type == VarType.STRING && v2.type == VarType.INT){
            LLVMGenerator.int_to_string(v2.name,BUFFER_SIZE);
            v2.name = "%" + (LLVMGenerator.reg - 2);
            LLVMGenerator.add_string(v1.name, v1.length, v2.name, BUFFER_SIZE);
            stack.push(new Value("%" + (LLVMGenerator.reg - 3), VarType.STRING, v1.length));
        } else if (v1.type == VarType.INT && v2.type == VarType.STRING){
            LLVMGenerator.string_to_int(v2.name);
            LLVMGenerator.add_i32(v1.name, "%"+(LLVMGenerator.reg - 1));
            stack.push(new Value("%"+(LLVMGenerator.reg - 1),VarType.INT, 0));
        } else {
            error(ctx.getStart().getLine(), "add type mismatch " + v1.type + " " + v2.type);
        }

    }

    @Override
    public void exitSub(gramParser.SubContext ctx) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
         if (v1.type == VarType.INT) {
               LLVMGenerator.sub_i32(v1.name,v2.name);
               stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT,0));
         }
         else if (v1.type == VarType.REAL) {
               LLVMGenerator.sub_double(v1.name,v2.name);
               stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL,0));
               
         }
         else{
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
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT,0));
            }
            if (v1.type == VarType.REAL) {
                LLVMGenerator.mult_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL,0));
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
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT,0));
            }
            if (v1.type == VarType.REAL) {
                LLVMGenerator.div_double(v2.name, v1.name);
                stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL,0));
            }
        } else {
            error(ctx.getStart().getLine(), "division type mismatch");
        }

    }

    @Override
    public void exitToint(gramParser.TointContext ctx) {
        Value v = stack.pop();
        LLVMGenerator.fptosi(v.name);
        stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT,0));
    }

    @Override
    public void exitToreal(gramParser.TorealContext ctx) {
        Value v = stack.pop();
        LLVMGenerator.sitofp(v.name);
        stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.REAL,0));
    }

    @Override
    public void exitPrint(gramParser.PrintContext ctx) {
        String ID = ctx.ID().getText();
        Value v = variables.get(ID);
        if (v.type != null) {
            if (v.type == VarType.INT) {
                LLVMGenerator.printf_i32(ID);
            }
            if (v.type == VarType.REAL) {
                LLVMGenerator.printf_double(ID);
            }
            if (v.type == VarType.STRING){
                LLVMGenerator.printf_string(ID);
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
            LLVMGenerator.scanf_double(ID); 
        } else if (v.type == VarType.STRING){
            LLVMGenerator.scanf_string(ID, BUFFER_SIZE);
        }

    }

    void error(int line, String msg) {
        System.err.println("Error, line " + line + ", " + msg);
        System.exit(1);
    }


}