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
        
        File outFile = new File( inFile.getPath().concat(".vm") ); // Filepath[.jack]->Filepath[.vm]
        
        try {
            outFile.createNewFile();
            writer = new FileWriter(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void closeWriter()
    {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void compileClass()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<Class>");
        writeLine(sb.toString());
        indintationCount++;

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS.");
        if(tokenizer.tokenType() != TokenType.KEYWORD)
            throw new RuntimeException("Not CLASS start.");
        if(!tokenizer.keyword().equals("class"))
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
        writeLine(sb.toString());

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
        writeLine(sb.toString());

        writeKeyword();

        //Void or Type
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.KEYWORD && tokenizer.tokenType() != TokenType.IDENTIFIER)
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
        writeLine(sb.toString());


        //Type
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for ParamList.");
        if(tokenizer.tokenType() == TokenType.SYMBOL)
            return;

        if(tokenizer.tokenType() != TokenType.KEYWORD && tokenizer.tokenType() != TokenType.IDENTIFIER)
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

                if( (tokenizer.tokenType() != TokenType.SYMBOL) && (tokenizer.symbol() != ',') )
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
        writeLine(sb.toString());
        while (tokenizer.keyword().equals("var")) //varDec*
        {
            writeKeyword();
            //Type
            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.KEYWORD && tokenizer.tokenType() != TokenType.IDENTIFIER)
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
        writeLine(sb.toString());

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
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<DoStatement>");
        indintationCount++;
        writeLine(sb.toString());

        writeKeyword(); // Do

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for DO_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("do statement subroutine/var/class name missing");
        compileSubroutineCall();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for DO_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("do statement closer missing.");
        writeSymbol();

        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</DoStatement>");
        writeLine(sb.toString());
    }

    public void compileLet()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<LetStatement>");
        indintationCount++;
        writeLine(sb.toString());

        writeKeyword(); // Let

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("let statement variable name Missing");
        writeIdentifier();


        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("let statement invalid end");
        writeSymbol();

        if(tokenizer.symbol() == '[')
        {
            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for LET_STATEMENT.");
            TokenType type = tokenizer.tokenType();
            if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
                throw new RuntimeException("let statement invalid end");
            compileExpression(); // WIP
            //Symbol ACCESS
            writeSymbol();

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for LET_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.SYMBOL)
                throw new RuntimeException("let statement invalid end");
            writeSymbol();
        }

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        TokenType type = tokenizer.tokenType();
        if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
            throw new RuntimeException("let statement invalid end");
        compileExpression(); //WIP

        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("let statement missing closer. ';' missing. ");
        writeSymbol(); // ;

        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</LetStatement>");
        writeLine(sb.toString());
    }

    public void compileWhile()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<LetStatement>");
        indintationCount++;
        writeLine(sb.toString());

        writeKeyword(); // While

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
            throw new RuntimeException("while statement missing condition parenthesis");
        writeSymbol();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        TokenType type = tokenizer.tokenType();
        if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
            throw new RuntimeException("let statement invalid end");
        compileExpression(); //WIP

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
            throw new RuntimeException("while statement missing condition parenthesis");
        writeSymbol();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='{')
            throw new RuntimeException("while statement missing body brace");
        writeSymbol();
        compileStatements();
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='}')
            throw new RuntimeException("while statement missing body brace");
        writeSymbol();


        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</LetStatement>");
        writeLine(sb.toString());
    }

    public void compileReturn()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<LetStatement>");
        indintationCount++;
        writeLine(sb.toString());

        writeKeyword(); // Return

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for RETURN_STATEMENT.");

        TokenType type = tokenizer.tokenType();
        if(type == TokenType.KEYWORD || type == TokenType.IDENTIFIER || type == TokenType.INT_CONST || type == TokenType.STRING_CONST || (type == TokenType.SYMBOL && tokenizer.symbol() != ';'))
        {
            compileExpression(); //WIP
        }
        
        if(type == TokenType.SYMBOL && tokenizer.symbol() == ';')
            writeSymbol();
        else
            throw new RuntimeException("No more tokens for RETURN_STATEMENT.");

        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</LetStatement>");
        writeLine(sb.toString());
    }

    public void compileIf()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<LetStatement>");
        indintationCount++;
        writeLine(sb.toString());

        writeKeyword(); // IF
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for IF_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
            throw new RuntimeException("while statement missing condition parenthesis");
        writeSymbol();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for IF_STATEMENT.");
        compileExpression(); //WIP

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
            throw new RuntimeException("while statement missing condition parenthesis");
        writeSymbol();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for IF_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='{')
            throw new RuntimeException("while statement missing body brace");
        writeSymbol();
        compileStatements();
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for IF_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='}')
            throw new RuntimeException("while statement missing body brace");
        writeSymbol();

        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</LetStatement>");
        writeLine(sb.toString());
    }

    private void compileSubroutineCall()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<SubRoutineCall>");
        indintationCount++;
        writeLine(sb.toString());
        
        writeIdentifier();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for SUBROUTINECALL_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("sub routine syntax error");
        writeSymbol();

        if(tokenizer.symbol() == '.')
        {
            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for SUBROUTINECALL_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                throw new RuntimeException("sub routine name is missing error");
            writeIdentifier();

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for SUBROUTINECALL_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
                throw new RuntimeException("sub missing condition parenthesis");
            writeSymbol();
        }

        compileExpressionList();

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
             throw new RuntimeException("sub routine missing condition parenthesis");
        writeSymbol();

        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</SubRoutineCall>");
        writeLine(sb.toString());
    }

    public void compileExpression()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<Expression>");
        indintationCount++;
        writeLine(sb.toString());

        compileTerm();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for EXPRESSION.");
        while(tokenizer.tokenType() == TokenType.SYMBOL && isOP(tokenizer.symbol()) )
        {
            writeSymbol();

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for EXPRESSION.");
            TokenType type = tokenizer.tokenType();
            if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
                throw new RuntimeException("expression op requires term");
            compileTerm();

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for EXPRESSION.");
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

        switch (tokenizer.tokenType()) 
        {
            case IDENTIFIER:
                TermIdentifierChecks();
                break;
            case INT_CONST: 
                writeIntegerConst();
                break;
            case STRING_CONST:
                writeStringConst();
                break;
            case KEYWORD:    
                if( !tokenizer.keyword().equals("true") ||  !tokenizer.keyword().equals("false") ||  !tokenizer.keyword().equals("null") || !tokenizer.keyword().equals("this"))
                    throw new RuntimeException("not a keyword constant");                    
                break;
            case SYMBOL:
                TermSymbolChecks();
                break;
            default:
                throw new RuntimeException("not a term token");
        }

        sb = new StringBuilder();
        indintationCount--;
        sb.append(" ".repeat(indintationCount));
        sb.append("</Term>");
        writeLine(sb.toString());
    }

    private void TermSymbolChecks()
    {
        switch (tokenizer.symbol()) {
            //Expresion Term
            case '(':
                writeSymbol();
                compileExpression();
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
        writeIdentifier();
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
                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for TERM.");
                if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                    throw new RuntimeException("subroutine name missing.");
                writeIdentifier();

                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for SUBROUTINECALL_STATEMENT.");
                if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
                    throw new RuntimeException("sub missing condition parenthesis");
                writeSymbol();
                compileExpressionList();
                
                if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
                    throw new RuntimeException("sub routine missing condition parenthesis");
                writeSymbol();
                break;
        }

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

    private void compileExpressionList()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indintationCount));
        sb.append("<ExpressionList>");
        indintationCount++;
        writeLine(sb.toString());

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for EXPRESSION_LIST.");
        TokenType type = tokenizer.tokenType();
        if(type == TokenType.KEYWORD || type == TokenType.IDENTIFIER || type == TokenType.INT_CONST || type == TokenType.STRING_CONST )
        {
            compileExpression();

            if(tokenizer.tokenType() == TokenType.SYMBOL)
                while (tokenizer.symbol() == ',') 
                {
                    writeSymbol();
                    
                    if(!tokenizer.ifTokensAdvance())
                        throw new RuntimeException("No more tokens for EXPRESSION_LIST.");
                    if(type != TokenType.KEYWORD || type != TokenType.IDENTIFIER || type != TokenType.INT_CONST || type != TokenType.STRING_CONST)
                        throw new RuntimeException("expresion list, opened list.");
                    compileExpression();

                    if(!tokenizer.ifTokensAdvance())
                        throw new RuntimeException("No more tokens for EXPRESSION_LIST.");
                    if(tokenizer.tokenType() != TokenType.SYMBOL)
                        break;
                }
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
