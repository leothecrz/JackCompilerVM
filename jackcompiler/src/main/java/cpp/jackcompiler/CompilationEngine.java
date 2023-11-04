package cpp.jackcompiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine 
{

    private int indintationCount;
    private JackTokenizer tokenizer;
    private FileWriter writer;

    //In file should be confirm as .jack outisde of engine. outfile should also be assured to not exist.
    public CompilationEngine(File inFile)
    {
        indintationCount = 0;
        
        tokenizer = new JackTokenizer(inFile);
        
        File outFile = new File( inFile.getPath().concat("/").concat( inFile.getName() ).concat(".vm") ); // Filepath[.jack]->Filepath[.vm]
        
        try {
            outFile.createNewFile();
            writer = new FileWriter(inFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void compileClass()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<Class>");
        indintationCount++;

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS.");
        if(tokenizer.tokenType() != TokenType.KEYWORD)
            throw new RuntimeException("Not CLASS start.");
        if(tokenizer.keyword() != "class")
            throw new RuntimeException("keyword CLASS missing.");
        writeKeyword();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Missing CLASS name.");
        writeIdentifier();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("Missing CLASS open bracket.");
        writeSymbol();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS.");

        while( (tokenizer.tokenType() == TokenType.KEYWORD) && 
            (tokenizer.keyword().equals("static") || tokenizer.keyword().equals("field") )  )
        {
            compileClassVarDec(); //return after advancing post declaration
        }

        while( (tokenizer.tokenType() == TokenType.KEYWORD) && 
            (tokenizer.keyword().equals("constructor") || tokenizer.keyword().equals("function") || tokenizer.keyword().equals("method") ) )  
        {
            compileSubroutine(); //return after advancing post declaration
        }

        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("Missing CLASS end bracket.");
        writeSymbol();

        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</Class>");
        writeLine(sb.toString());
    }

    public void compileClassVarDec()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<ClassVarDec>");
        indintationCount++;

        writeKeyword();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
        if(tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Variable Type Missing");
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            {
                String key = tokenizer.keyword();
                if(key.equals("int") | key.equals("char") | key.equals("boolean"))
                    writeKeyword();
                else
                    throw new RuntimeException("Variable Type INVALID");
            }
        else
            writeIdentifier();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Variable Name Missing");
        writeIdentifier();
            
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("Invalid var declaration end.");
        
        while ( tokenizer.symbol() == ',' ) 
        {
            writeSymbol();
            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                throw new RuntimeException("Variable Name Missing");
            writeIdentifier();

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
        }
        
        if(tokenizer.symbol() == ';')
        {
            writeSymbol();
        }
        else
            throw new RuntimeException("Missing statement closer. ';'. ");

        if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for CLASS from (CVD)");

        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</ClassVarDec>");
        writeLine(sb.toString());
    }

    public void compileSubroutine()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<Subroutine>");
        indintationCount++;

        writeKeyword();

        //Void or Type
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Subroutine Return Type Missing");
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            {
                String key = tokenizer.keyword();
                if(key.equals("void") | key.equals("int") | key.equals("char") | key.equals("boolean"))
                    writeKeyword();
                else
                    throw new RuntimeException("Subroutine Return Type INVALID");
            }
        else
            writeIdentifier();

        //SubName
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Subroutine Name Missing");
        writeIdentifier();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() == '(')
            throw new RuntimeException("SUB ROUTINE Opening parenthesis missing.");
        writeSymbol();

        compileParameterList();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() == ')')
            throw new RuntimeException("SUB ROUTINE Opening parenthesis missing.");
        writeSymbol();
        //PARAMS END
        //BODY START
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() == '{')
            throw new RuntimeException("SUB ROUTINE Opening parenthesis missing.");
        writeSymbol();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            compileVarDec();
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            compileStatements();
        /////////////RETURN POINT FOR STATEMENTS
        if(tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() == '}')
            throw new RuntimeException("SUB ROUTINE Closing parenthesis missing.");
        writeSymbol();
        //BODY END

        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</Subroutine>");
        writeLine(sb.toString());
    }

    public void compileParameterList()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<Parameter List>");
        indintationCount++;

        if(!tokenizer.ifTokensAdvance())
        {   
            writeLine("<No Parameters/>");
            return;
        }

        //Type
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for ParamList.");
        if(tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Var Type Missing");
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            {
                String key = tokenizer.keyword();
                if(key.equals("int") | key.equals("char") | key.equals("boolean"))
                    writeKeyword();
                else
                    throw new RuntimeException("Variable Type INVALID");
            }
        else
            writeIdentifier();

        //VarName
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for ParamList.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Var Name Missing");
        writeIdentifier();
        
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for ParamList.");
        if(tokenizer.tokenType() == TokenType.SYMBOL)
            while ( tokenizer.symbol() == ',' ) 
            {
                writeSymbol();
                //Type
                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for ParamList.");
                if(tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.tokenType() != TokenType.IDENTIFIER)
                    throw new RuntimeException("Var Type Missing");
                if(tokenizer.tokenType() == TokenType.KEYWORD)
                    {
                        String key = tokenizer.keyword();
                        if(key.equals("int") | key.equals("char") | key.equals("boolean"))
                            writeKeyword();
                        else
                            throw new RuntimeException("Variable Type INVALID");
                    }
                else
                    writeIdentifier();

                //Name
                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for ParamList.");
                if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                    throw new RuntimeException("Variable Name Missing");
                writeIdentifier();
                
                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for ParamList.");

                if( (tokenizer.tokenType() != TokenType.SYMBOL) | (tokenizer.symbol() != ',') )
                    break;
            }
        

        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</Parameter List>");
        writeLine(sb.toString());
    }

    public void compileVarDec()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<VarDec>");
        indintationCount++;
        while (tokenizer.keyword().equals("var")) //varDec*
        {
            writeKeyword();
            //Type
            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.tokenType() != TokenType.IDENTIFIER)
                throw new RuntimeException("varDEC Type Missing");
            if(tokenizer.tokenType() == TokenType.KEYWORD)
            {
                String key = tokenizer.keyword();
                if(key.equals("int") | key.equals("char") | key.equals("boolean"))
                    writeKeyword();
                else
                    throw new RuntimeException("varDEC Return Type INVALID");
            }
            else
                writeIdentifier();

            //varName
            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                throw new RuntimeException("varDEC Name Missing");
            writeIdentifier();

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.SYMBOL)
                throw new RuntimeException("Invalid var declaration end.");
            
            while ( tokenizer.symbol() == ',' ) 
            {
                writeSymbol();
                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
                if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                    throw new RuntimeException("Variable Name Missing");
                writeIdentifier();

                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
            }
            if(tokenizer.symbol() == ';')
            {
                writeSymbol();
            }
            else
                throw new RuntimeException("Missing statement closer. ';'. ");

            if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE from (VD)");

            if(tokenizer.tokenType() != TokenType.KEYWORD)
                break;    
        }
        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</VarDec>");
        writeLine(sb.toString());
    }

    public void compileStatements()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<Statements>");
        indintationCount++;
        while (tokenizer.tokenType() == TokenType.KEYWORD) 
        {
            switch (tokenizer.keyword()) 
            {
                case "let":
                    compileLet();
                    break;
                case "if":
                    compileIf();
                    break;
                case "while":
                    compileWhile();
                    break;
                case "do":
                    compileWhile();
                    break;
                case "return":
                    compileReturn();
                    break;
                default:
                    throw new RuntimeException("Invalid Statement Start");
            }    
        }
        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</Statements>");
        writeLine(sb.toString());
    }

    public void compileDo()
    {

    }

    public void compileLet()
    {

    }

    public void compileWhile()
    {

    }

    public void compileReturn()
    {

    }

    public void compileIf()
    {

    }

    public void compileExpression()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<Expression>");
        indintationCount++;

        compileTerm();
        if(!tokenizer.hasMoreTokens())
            return;

        tokenizer.advance();
        while((tokenizer.tokenType() == TokenType.SYMBOL) && isOP(tokenizer.symbol()))
        {
            writeSymbol();
            tokenizer.advance();
            compileTerm();
        }

        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</Expression>");
        writeLine(sb.toString());
    }

    public void compileTerm()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<Term>");
        writeLine(sb.toString());

        indintationCount++;
        switch (tokenizer.tokenType()) {
            case INT_CONST:
                writeIntegerConst();
                break;

            case STRING_CONST:
                writeStringConst();
                break;
            
            case KEYWORD: // should only be called for 'true' 'false' 'this' 'null'
                writeKeyword();
                break;
            
            case IDENTIFIER:
                writeIdentifier();
                if(tokenizer.hasMoreTokens())    
                    TermIdentifierChecks();
                break;

            case SYMBOL:
                TermSymbolChecks();
                break;
        
            default:
                break;
        }
        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</Term>");
        writeLine(sb.toString());
    }

    private boolean isOP(char c)
    {
        switch (c) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '&':
            case '|':
            case '<':
            case '>':
            case '=':
                return true;
            default:
                return false;
        }
    }

    private void TermSymbolChecks()
    {
        switch (tokenizer.symbol()) {
            //Expresiion Term
            case '(':
                writeSymbol();
                compileExpression();
                if(!tokenizer.ifTokensAdvance())
                {
                    //ERROR
                }
                if(tokenizer.symbol() == ')')
                    writeSymbol();
                else
                    //error
                break;
            //Unary Ops
            case '-':
            case '~':
                writeSymbol();
                if(!tokenizer.ifTokensAdvance())
                {
                    //ERROR
                }
                compileTerm();
                break;
        
            default:
                
            throw new RuntimeException("Symbol Not Applicable To Term");
        }
    }

    private void TermIdentifierChecks()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() != TokenType.SYMBOL)
        {
            return;
        }
            
        switch (tokenizer.symbol()) {
            case '[': // VARNAME[expressions]
                writeSymbol();
                if(!tokenizer.hasMoreTokens())
                {
                    System.err.println("TERM ARRAY ACCESS FAILURE");
                }
                compileExpression();
                tokenizer.advance();
                writeSymbol();
                break;

            case '(': //Subroutine Call
                writeSymbol();
                if(!tokenizer.hasMoreTokens())
                {
                    //missing expressions and ')'
                }
                compileExpressionList();
                tokenizer.advance();
                writeSymbol();
                break;

            case '.': //Subroutine Call
                writeSymbol();
                if(!tokenizer.hasMoreTokens())
                {

                }
                tokenizer.advance();
                writeIdentifier();
                
                tokenizer.advance();
                writeSymbol();

                tokenizer.advance();
                compileExpressionList();

                tokenizer.advance();
                writeSymbol();
                break;
        }

    }

    private void compileExpressionList()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<ExpressionList>");
        indintationCount++;

        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() != '(')
        {

        }


        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</ExpressionList>");
        writeLine(sb.toString());
    }

    private void writeKeyword()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<KeyWord>");

        sb.append(tokenizer.keyword());

        sb.append("</KeyWord>\n");
        writeLine(sb.toString());
    }

    private void writeSymbol()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<Symbol> ");

        sb.append(tokenizer.symbol());

        sb.append(" </Symbol>\n");
        writeLine(sb.toString());
    }

    private void writeIdentifier()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<Identifier> ");

        sb.append(tokenizer.identifier());

        sb.append(" </Identifier>\n");
        writeLine(sb.toString());
    }

    private void writeStringConst()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<StringConst> ");

        sb.append(tokenizer.stringVal());

        sb.append(" </StringConst>\n");
        writeLine(sb.toString());
    }

    private void writeIntegerConst()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<IntegerConst> ");

        sb.append( String.valueOf( tokenizer.intVal() ) );

        sb.append(" </IntegerConst>\n");
        writeLine(sb.toString());
    }

    private void writeLine(String str)
    {
        try {
            writer.write(str + "\n");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed To write to file");
        }
    }

    
}
