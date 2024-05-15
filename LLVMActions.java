
import java.util.HashSet;


public class LLVMActions extends gramBaseListener {

    HashSet<String> variables = new HashSet<String>();

    @Override 
    public void exitProg(gramParser.ProgContext ctx) { 
       System.out.println( LLVMGenerator.generate() );
    }
    
    @Override
    public void exitAssign(gramParser.AssignContext ctx) { 
       String ID = ctx.ID().getText();
       String INT = ctx.INT().getText();
       if( ! variables.contains(ID) ) {
          variables.add(ID);
          LLVMGenerator.declare(ID);          
       } 
       LLVMGenerator.assign(ID, INT);
    }

    @Override
    public void exitPrint(gramParser.PrintContext ctx) {
       String ID = ctx.ID().getText();
       if( variables.contains(ID) ) {
          LLVMGenerator.printf( ID );
       } else {
          System.err.println("Line "+ ctx.getStart().getLine()+", unknown variable: "+ID);
       }
    } 

    @Override
    public void exitRead(gramParser.ReadContext ctx) {
       String ID = ctx.ID().getText();
       if( ! variables.contains(ID) ) {
          variables.add(ID);
          LLVMGenerator.declare(ID);          
       } 
       LLVMGenerator.scanf(ID);
    } 


}