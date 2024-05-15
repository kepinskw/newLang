import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

enum VarType{ INT, REAL, UNKNOWN}

class Value{
         public String name;
         public VarType type;
         public Value(String name, VarType type){
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
       System.out.println( LLVMGenerator.generate() );
    }
    
    @Override
    public void exitAssign(gramParser.AssignContext ctx) { 
       String ID = ctx.ID().getText();
       Value  v  = stack.pop();
       variables.put(ID, v.type);
       if(v.type == VarType.INT){
         LLVMGenerator.declare_i32(ID);
         LLVMGenerator.assign_i32(ID, v.name);
       }
       if(v.type == VarType.REAL){
         LLVMGenerator.declare_double(ID);
         LLVMGenerator.assign_double(ID, v.name);
       }
    }

    @Override
    public void exitInt(gramParser.IntContext ctx){
       stack.push(new Value(ctx.INT().getText(),VarType.INT));
    }

    @Override
    public void exitReal(gramParser.RealContext ctx){
       stack.push(new Value(ctx.REAL().getText(),VarType.REAL));
    }
   
   @Override
   public void exitAdd(gramParser.AddContext ctx){
      Value v1 = stack.pop();
      Value v2 = stack.pop();
      
      if(v1.type == v2.type){
         if(v1.type == VarType.INT){
            LLVMGenerator.add_i32(v1.name, v2.name);
            stack.push(new Value("%" + (LLVMGenerator.reg - 1), VarType.INT));
         }
         if(v1.type == VarType.REAL){
            LLVMGenerator.add_double(v1.name, v2.name); 
             stack.push( new Value("%"+(LLVMGenerator.reg - 1), VarType.REAL) ); 
         }
         else{
            error(ctx.getStart().getLine(), "add type mismatch");
         }
      }
   }

   @Override
   public void exitMul(gramParser.MulContext ctx){
      Value v1 = stack.pop();
      Value v2 = stack.pop();

      if(v1.type == v2.type){
         if(v1.type == VarType.INT){
            LLVMGenerator.mult_i32(v1.name, v2.name);
            stack.push(new Value("%"+(LLVMGenerator.reg - 1), VarType.INT));
         }
         if(v1.type == VarType.REAL){
            LLVMGenerator.mult_double(v1.name,v2.name);
            stack.push(new Value("%"+(LLVMGenerator.reg - 1),  VarType.REAL));
         }
         else{
            error(ctx.getStart().getLine(), "multi type mismatch");
         }
      }
   }

   @Override
   public void exitToint(gramParser.TointContext ctx){
      Value v = stack.pop();
      LLVMGenerator.fptosi(v.name);
      stack.push(new Value("%"+(LLVMGenerator.reg - 1), VarType.INT));
   }

   @Override
   public void exitToreal(gramParser.TorealContext ctx){
      Value v = stack.pop();
      LLVMGenerator.sitofp(v.name);
      stack.push(new Value("%" + (LLVMGenerator.reg - 1),VarType.REAL));
   }

    @Override
    public void exitPrint(gramParser.PrintContext ctx) {
      //  String ID = ctx.ID().getText();
      //  if( variables.contains(ID) ) {
      //     LLVMGenerator.printf( ID );
      //  } else {
      //     System.err.println("Line "+ ctx.getStart().getLine()+", unknown variable: "+ID);
      //  }
    } 

    @Override
    public void exitRead(gramParser.ReadContext ctx) {
      //  String ID = ctx.ID().getText();
      //  if( ! variables.contains(ID) ) {
      //     variables.add(ID);
      //     LLVMGenerator.declare(ID);          
      //  } 
      //  LLVMGenerator.scanf(ID);
    }
    
    void error(int line, String msg){
      System.err.println("Error, line "+line+", "+msg);
      System.exit(1);
  } 


}