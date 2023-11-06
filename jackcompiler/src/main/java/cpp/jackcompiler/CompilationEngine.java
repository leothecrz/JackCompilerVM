package cpp.jackcompiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine 
{

    private int indentationCount;
    private JackTokenizer tokenizer;
    private FileWriter writer;

    //In file should be confirm as .jack outisde of engine. outfile should also be assured to not exist.
    public CompilationEngine(File inFile)
    {
        indentationCount = 0;
        
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
        openTagAndIncrementIndent("<Class>");

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS.");
        if(tokenizer.tokenType() != TokenType.KEYWORD)
            throw new RuntimeException("Not CLASS start.");
        if(!tokenizer.keyword().equals("class"))
            throw new RuntimeException("keyword CLASS missing.");
        writeKeyword(); //CLASS

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Missing CLASS name.");
        writeIdentifier(); //CLASS NAME

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("Missing CLASS open bracket.");
        writeSymbol(); // {

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
        writeSymbol(); // }

        closeTagAndDecrementIndent("</Class>");
    }

    public void compileClassVarDec()
    {
        openTagAndIncrementIndent("<ClassVarDec>");

        writeKeyword(); // static or field

        //TYPE
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
        writeIdentifier(); // VAR NAME
            
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("Invalid var declaration end.");
        
        while ( tokenizer.symbol() == ',' ) 
        {
            writeSymbol(); // ,

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                throw new RuntimeException("Variable Name Missing");
            writeIdentifier(); // VAR NAME

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.SYMBOL)
                throw new RuntimeException("Missing statement closer. ';'. ");
        }

        if(tokenizer.symbol() != ';')
            throw new RuntimeException("Missing statement closer. ';'. ");
        writeSymbol(); // ;

        if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for CLASS from (CVD)");

        closeTagAndDecrementIndent("</ClassVarDec>");
    }

    public void compileSubroutine()
    {
        openTagAndIncrementIndent("<Subroutine>");

        writeKeyword(); // Constructor Function Method

        //Void or TYPE
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

        //NAME
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Subroutine Name Missing");
        writeIdentifier();

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(')
            throw new RuntimeException("SUB ROUTINE Opening parenthesis missing.");
        writeSymbol(); // (

        compileParameterList(); // PARAMLIST

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')')
            throw new RuntimeException("SUB ROUTINE Opening parenthesis missing.");
        writeSymbol(); // )
        
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{')
            throw new RuntimeException("SUB ROUTINE Opening parenthesis missing.");
        writeSymbol(); // {

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            compileVarDec();
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            compileStatements();

        if(tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() == '}')
            throw new RuntimeException("SUB ROUTINE Closing parenthesis missing.");
        writeSymbol(); // }
        
        closeTagAndDecrementIndent("</Subroutine>");
    }

    public void compileParameterList()
    {
        openTagAndIncrementIndent("<Parameter List>");

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for ParamList.");
        if(tokenizer.tokenType() == TokenType.SYMBOL)
        {
            closeTagAndDecrementIndent("</Parameter List>");
            return;
        }
            

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
        //Type

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for ParamList.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Var Name Missing");
        writeIdentifier(); //VarName
        
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for ParamList.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("invalid parameter list end.");

        while ( tokenizer.symbol() == ',' ) 
        {
            writeSymbol(); // ,

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
                writeIdentifier(); //Type

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for ParamList.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                throw new RuntimeException("Variable Name Missing");
            writeIdentifier(); //Name
            
            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for ParamList.");
            if( (tokenizer.tokenType() != TokenType.SYMBOL))
                break;
        }

        closeTagAndDecrementIndent("</Parameter List>");
    }

    public void compileVarDec()
    {
        openTagAndIncrementIndent("<VarDec>");

        while (tokenizer.keyword().equals("var")) //varDec*
        {
            writeKeyword();
            
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
                writeIdentifier(); //Type

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                throw new RuntimeException("varDEC Name Missing");
            writeIdentifier(); //varName

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.SYMBOL)
                throw new RuntimeException("Invalid var declaration end.");
            
            while ( tokenizer.symbol() == ',' ) 
            {
                writeSymbol(); // ,

                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for CLASS_VAR_DEC.");
                if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                    throw new RuntimeException("Variable Name Missing");
                writeIdentifier(); // VarName

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

        closeTagAndDecrementIndent("</VarDec>");
    }

    public void compileStatements()
    {
        openTagAndIncrementIndent("<Statements>");

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
                    compileDo();
                    break;
                case "return":
                    compileReturn();
                    break;
                default:
                    throw new RuntimeException("Invalid Statement Start");
            }

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for STATEMENTS.");
        }

        closeTagAndDecrementIndent("</Statements>");
    }

    public void compileDo()
    {
        openTagAndIncrementIndent("<DoStatement>");

        writeKeyword(); // Do

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for DO_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("do statement subroutine/var/class name missing");
        compileSubroutineCall();

        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("do statement closer missing.");
        writeSymbol(); // ;

        closeTagAndDecrementIndent("</DoStatement>");
    }

    public void compileLet()
    {
        openTagAndIncrementIndent("<LetStatement>");

        writeKeyword(); // Let

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("let statement variable name Missing");
        writeIdentifier();// varName

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("let statement invalid end");
        writeSymbol(); // '=' or '['

        if(tokenizer.symbol() == '[')
        {
            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for LET_STATEMENT.");
            TokenType type = tokenizer.tokenType();
            if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
                throw new RuntimeException("let statement invalid end");
            compileExpression(); // WIP
            
            writeSymbol(); // ']'

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for LET_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.SYMBOL)
                throw new RuntimeException("let statement invalid end");
            writeSymbol(); // '='
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

        closeTagAndDecrementIndent("</LetStatement>");
    }

    public void compileWhile()
    {
        openTagAndIncrementIndent("<WhileStatement>");

        writeKeyword(); // While

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
            throw new RuntimeException("while statement missing condition parenthesis");
        writeSymbol(); // (

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        TokenType type = tokenizer.tokenType();
        if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
            throw new RuntimeException("let statement invalid end");
        compileExpression(); //WIP

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
            throw new RuntimeException("while statement missing condition parenthesis");
        writeSymbol(); // )

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='{')
            throw new RuntimeException("while statement missing body brace");
        writeSymbol(); // {

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            compileStatements();

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='}')
            throw new RuntimeException("while statement missing body brace");
        writeSymbol(); // }

        closeTagAndDecrementIndent("</WhileStatement>");
    }

    public void compileReturn()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<LetStatement>");
        indentationCount++;
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
        indentationCount--;
        sb.append("\t".repeat(indentationCount));
        sb.append("</LetStatement>");
        writeLine(sb.toString());
    }

    public void compileIf()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<LetStatement>");
        indentationCount++;
        writeLine(sb.toString());

        writeKeyword(); // IF
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for IF_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
            throw new RuntimeException("while statement missing condition parenthesis");
        writeSymbol(); // (

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for IF_STATEMENT.");
        compileExpression(); //WIP

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
            throw new RuntimeException("while statement missing condition parenthesis");
        writeSymbol(); // )

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for IF_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='{')
            throw new RuntimeException("while statement missing body brace");
        writeSymbol(); // {

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for IF_STATEMENT.");
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            compileStatements(); //WIP

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='}')
            throw new RuntimeException("while statement missing body brace");
        writeSymbol(); // }

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for IF_STATEMENT.");
        
        if(tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyword().equals("else"))
        {
            writeKeyword(); // ELSE
            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for IF_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='{')
                throw new RuntimeException("while statement missing body brace");
            writeSymbol(); // {

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for IF_STATEMENT.");
            if(tokenizer.tokenType() == TokenType.KEYWORD)
                compileStatements(); //WIP

            if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='}')
                throw new RuntimeException("while statement missing body brace");
            writeSymbol(); // }

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for IF_STATEMENT.");
        }

        sb = new StringBuilder();
        indentationCount--;
        sb.append("\t".repeat(indentationCount));
        sb.append("</LetStatement>");
        writeLine(sb.toString());
    }

    private void compileSubroutineCall()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<SubRoutineCall>");
        indentationCount++;
        writeLine(sb.toString());
        
        writeIdentifier(); //Name

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for SUBROUTINECALL_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            throw new RuntimeException("sub routine syntax error");
        writeSymbol(); // '(' or '.'

        if(tokenizer.symbol() == '.')
        {
            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for SUBROUTINECALL_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                throw new RuntimeException("sub routine name is missing error");
            writeIdentifier(); // NAME

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for SUBROUTINECALL_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
                throw new RuntimeException("sub missing condition parenthesis");
            writeSymbol(); //(
        }

        compileExpressionList();

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
             throw new RuntimeException("sub routine missing condition parenthesis");
        writeSymbol(); // )

        if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for SUBROUTINECALL_STATEMENT.");

        sb = new StringBuilder();
        indentationCount--;
        sb.append("\t".repeat(indentationCount));
        sb.append("</SubRoutineCall>");
        writeLine(sb.toString());
    }

    public void compileExpression()
    {
        openTagAndIncrementIndent("<Expression>");

        compileTerm();

        while(tokenizer.tokenType() == TokenType.SYMBOL && isOP(tokenizer.symbol()) )
        {
            writeSymbol();

            if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for EXPRESSION.");
            TokenType type = tokenizer.tokenType();
            if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
                throw new RuntimeException("expression op requires term");
            compileTerm();
        }
        
        closeTagAndDecrementIndent("</Expression>");
    }

    public void compileTerm()
    {
        openTagAndIncrementIndent("</Term>");

        switch (tokenizer.tokenType()) 
        {
            case IDENTIFIER:
                TermIdentifierChecks();
                break;
            case INT_CONST: 
                writeIntegerConst();
                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for TERM.");
                break;
            case STRING_CONST:
                writeStringConst();
                if(!tokenizer.ifTokensAdvance())
                throw new RuntimeException("No more tokens for TERM.");
                break;
            case KEYWORD:    
                if( !tokenizer.keyword().equals("true") ||  !tokenizer.keyword().equals("false") ||  !tokenizer.keyword().equals("null") || !tokenizer.keyword().equals("this"))
                    throw new RuntimeException("not a keyword constant");    
                writeKeyword();
                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for TERM.");
                break;
            case SYMBOL:
                TermSymbolChecks();
                break;
            default:
                throw new RuntimeException("not a term token");
        }

        closeTagAndDecrementIndent("</Term>");

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
                    throw new RuntimeException("not a term token");

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
        
            default: // pass check up
        }
    }

    private void TermIdentifierChecks()
    {
        writeIdentifier();

        if(!tokenizer.ifTokensAdvance())
        {
        }

        if(tokenizer.tokenType() != TokenType.SYMBOL)
        {
            return;
        }
            
        switch (tokenizer.symbol()) {
            case '[': // VARNAME[expressions]
                writeSymbol();
                if(!tokenizer.ifTokensAdvance())
                {
                    System.err.println("TERM ARRAY ACCESS FAILURE");
                }
                compileExpression();
                writeSymbol();
                break;

            case '(': //Subroutine Call
                writeSymbol();
                if(!tokenizer.ifTokensAdvance())
                {
                    //missing expressions and ')'
                }
                compileExpressionList();
                writeSymbol();
                break;

            case '.': //Subroutine Call
                writeSymbol();
                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for TERM.");
                if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                    throw new RuntimeException("subroutine name missing.");
                writeIdentifier();

                if(!tokenizer.ifTokensAdvance())
                    throw new RuntimeException("No more tokens for TERM.");
                if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
                    throw new RuntimeException("sub missing condition parenthesis");
                writeSymbol();
                compileExpressionList();
                
                if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
                    throw new RuntimeException("sub routine missing condition parenthesis");
                writeSymbol();
                break;
            
            case ']':
            case ')':
                return;

            default:
                if(!isOP(tokenizer.symbol()))
                    throw new RuntimeException("invalid symbol for term");
                return;

        }
        
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for TERM.");
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
        openTagAndIncrementIndent("</ExpressionList>");

        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for EXPRESSION_LIST.");
        TokenType type = tokenizer.tokenType();
        if(type == TokenType.KEYWORD || type == TokenType.IDENTIFIER || type == TokenType.INT_CONST || type == TokenType.STRING_CONST || type == TokenType.SYMBOL )
        {
            compileExpression();

            if(tokenizer.tokenType() == TokenType.SYMBOL)
                while (tokenizer.symbol() == ',') 
                {
                    writeSymbol(); // ,
                    
                    if(!tokenizer.ifTokensAdvance())
                        throw new RuntimeException("No more tokens for EXPRESSION_LIST.");
                    if(type != TokenType.KEYWORD || type != TokenType.IDENTIFIER || type != TokenType.INT_CONST || type != TokenType.STRING_CONST || type == TokenType.SYMBOL)
                        throw new RuntimeException("expresion list, opened list.");
                    compileExpression();

                    if(!tokenizer.ifTokensAdvance())
                        throw new RuntimeException("No more tokens for EXPRESSION_LIST.");
                    if(tokenizer.tokenType() != TokenType.SYMBOL)
                        break;
                }
        }
        
        closeTagAndDecrementIndent("</ExpressionList>");
    }

    private void writeKeyword()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<KeyWord>");

        sb.append(tokenizer.keyword());

        sb.append("</KeyWord>\n");
        writeLine(sb.toString());
    }

    private void writeSymbol()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<Symbol> ");

        sb.append(tokenizer.symbol());

        sb.append(" </Symbol>\n");
        writeLine(sb.toString());
    }

    private void writeIdentifier()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<Identifier> ");

        sb.append(tokenizer.identifier());

        sb.append(" </Identifier>\n");
        writeLine(sb.toString());
    }

    private void writeStringConst()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<StringConst> ");

        sb.append(tokenizer.stringVal());

        sb.append(" </StringConst>\n");
        writeLine(sb.toString());
    }

    private void writeIntegerConst()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
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

    private void openTagAndIncrementIndent(String tag)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append(tag);
        sb.append("\n");
        writeLine(sb.toString());
        indentationCount++;
    }

    private void closeTagAndDecrementIndent(String tag)
    {
        StringBuilder sb = new StringBuilder();
        indentationCount--;
        sb.append("\t".repeat(indentationCount));
        sb.append(tag);
        sb.append("\n");
        writeLine(sb.toString());
    }

    
}
