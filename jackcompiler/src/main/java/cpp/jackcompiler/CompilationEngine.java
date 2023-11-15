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
        try 
        {
            outFile.createNewFile();
            writer = new FileWriter(outFile);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }        
    }

    public void closeWriter()
    {
        try 
        {
            writer.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public void compileClass()
    {
        openTagAndIncrementIndent("<Class>");

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("'class' keyword","No more tokens for CLASS.");
        if(tokenizer.tokenType() != TokenType.KEYWORD)
            tokenizer.printError("'class' keyword","Not CLASS start.");
        if(!tokenizer.keyword().equals("class"))
            tokenizer.printError("'class' keyword","keyword CLASS missing.");
        writeKeyword(); //CLASS

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("class identifier","No more tokens for CLASS.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("class identifier","Missing CLASS name.");
        writeIdentifier(); //CLASS NAME

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("class openbracket","No more tokens for CLASS.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            tokenizer.printError("class openbracket","Missing CLASS open bracket.");
        writeSymbol(); // {

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("class vardec/subroutines","No more tokens for CLASS.");
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
            tokenizer.printError("class closing bracket","Missing CLASS end bracket.");
        writeSymbol(); // }

        closeTagAndDecrementIndent("</Class>");
    }

    public void compileClassVarDec()
    {
        openTagAndIncrementIndent("<ClassVarDec>");

        writeKeyword(); // static or field

        compileType(false, "CLASS_VAR_DEC", "var");

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Class Var Dec Identifier","No more tokens for CLASS_VAR_DEC.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("Class Var Dec Identifier","Variable Name Missing");
        writeIdentifier(); // VAR NAME
            
        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Class Var Dec -Symbol","No more tokens for CLASS_VAR_DEC.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            tokenizer.printError("Class Var Dec -Symbol","Invalid var declaration end.");
        
        while ( tokenizer.symbol() == ',' ) 
        {
            writeSymbol(); // ,

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Class Var Dec - Identifier","No more tokens for CLASS_VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                tokenizer.printError("Class Var Dec - Identifier","Variable Name Missing");
            writeIdentifier(); // VAR NAME

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Class Var Dec - Symbol","No more tokens for CLASS_VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.SYMBOL)
                tokenizer.printError("Class Var Dec - Symbol","Missing statement closer. ';'. ");
        }

        if(tokenizer.symbol() != ';')
            tokenizer.printError("Class Var Dec - Symbol","Missing statement closer. ';'. ");
        writeSymbol(); // ;

        if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Class Var Dec - END","No more tokens for CLASS from (CVD)");

        closeTagAndDecrementIndent("</ClassVarDec>");
    }

    public void compileSubroutine()
    {
        openTagAndIncrementIndent("<Subroutine>");

        writeKeyword(); // Constructor Function Method

        compileType(true, "SUB_ROUTINE", "sub_route");
        
        //NAME
        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Class Subroutine - Identifier","No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("Class Subroutine - Identifier","Subroutine Name Missing");
        writeIdentifier();

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Class Subroutine - Open Parenthesis","No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '(')
            tokenizer.printError("Class Subroutine - Open Parenthesis","SUB ROUTINE Opening parenthesis missing.");
        writeSymbol(); // (

        compileParameterList(); // PARAMLIST

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != ')')
            tokenizer.printError("Class Subroutine - Closing Parenthesis","SUB ROUTINE Opening parenthesis missing.");
        writeSymbol(); // )
        
        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Class Subroutine - Opening Bracket","No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != '{')
            tokenizer.printError("Class Subroutine - Opening Bracket","SUB ROUTINE Opening parenthesis missing.");
        writeSymbol(); // {

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Class Subroutine - VarDec/Statemests","No more tokens for CLASS_SUB_ROUTE.");
        
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            compileVarDec();
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            compileStatements();

        if(tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() == '}')
            tokenizer.printError("Class Subroutine - Closing Bracket","SUB ROUTINE Closing parenthesis missing.");
        writeSymbol(); // }
        
        closeTagAndDecrementIndent("</Subroutine>");
    }

    public void compileParameterList()
    {
        openTagAndIncrementIndent("<Parameter List>");

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("ParameterList - Closing","No more tokens for ParamList.");
        if(tokenizer.tokenType() == TokenType.SYMBOL)
        {
            closeTagAndDecrementIndent("</Parameter List>");
            return;
        }
        if(tokenizer.tokenType() != TokenType.KEYWORD && tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("ParameterList - Var Type","Var Type Missing");
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            {
                String key = tokenizer.keyword();
                if(key.equals("int") | key.equals("char") | key.equals("boolean"))
                    writeKeyword();
                else
                    tokenizer.printError("ParameterList - Var Type","Variable Type INVALID");
            }
        else
            writeIdentifier(); //TYPE
        

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("ParameterList -Identifier","No more tokens for ParamList.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("ParameterList -Identifier","Var Name Missing");
        writeIdentifier(); //VarName
        
        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("ParameterList -Symbol","No more tokens for ParamList.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            tokenizer.printError("ParameterList -Symbol","invalid parameter list end.");

        while ( tokenizer.symbol() == ',' ) 
        {
            writeSymbol(); // ,

            compileType(false, "PARAM_LIST", "param");

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("ParameterList - Auxiliary identifiers","No more tokens for ParamList.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                tokenizer.printError("ParameterList - Auxiliary identifiers","Variable Name Missing");
            writeIdentifier(); //Name
            
            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("ParameterList - symbol","No more tokens for ParamList.");
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
            
            compileType(false, "VAR_DEC", "varDec");
        
            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Var Dec - Identifier ","No more tokens for VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                tokenizer.printError("Var Dec - Identifier ","varDEC Name Missing");
            writeIdentifier(); //varName

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Var Dec - Symbol","No more tokens for CLASS_VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.SYMBOL)
                tokenizer.printError("Var Dec - Symbol","Invalid var declaration end.");
            
            while ( tokenizer.symbol() == ',' ) 
            {
                writeSymbol(); // ,

                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("Var Dec - Auxiliary Identifier","No more tokens for CLASS_VAR_DEC.");
                if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                    tokenizer.printError("Var Dec - Auxiliary Identifier","Variable Name Missing");
                writeIdentifier(); // VarName

                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("Var Dec - Symbol","No more tokens for CLASS_VAR_DEC.");
            }

            if(tokenizer.symbol() == ';')
            {
                writeSymbol();
            }
            else
                tokenizer.printError("Var Dec - Symbol","Missing statement closer. ';'. ");

            if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("Var Dec - END","No more tokens for CLASS_SUB_ROUTE from (VD)");

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
                    tokenizer.printError("Statement - Start","Invalid Statement Start");
            }

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Statement - Start","No more tokens for STATEMENTS.");
        }

        closeTagAndDecrementIndent("</Statements>");
    }

    public void compileDo()
    {
        openTagAndIncrementIndent("<DoStatement>");

        writeKeyword(); // Do

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement Do - Call Identifier","No more tokens for DO_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("Statement Do - Call Identifier","do statement subroutine/var/class name missing");
        compileSubroutineCall();

        if(tokenizer.tokenType() != TokenType.SYMBOL)
            tokenizer.printError("Statement Do - Symbol","do statement closer missing.");
        writeSymbol(); // ;

        closeTagAndDecrementIndent("</DoStatement>");
    }

    public void compileLet()
    {
        openTagAndIncrementIndent("<LetStatement>");

        writeKeyword(); // Let

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement Let- Identifier","No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("Statement Let- Identifier","let statement variable name Missing");
        writeIdentifier();// varName

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement Let- Symbol","No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            tokenizer.printError("Statement Let- Symbol","let statement invalid end");
        writeSymbol(); // '=' or '['

        if(tokenizer.symbol() == '[')
        {
            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Statement Let Array- Expression","No more tokens for LET_STATEMENT.");
            TokenType type = tokenizer.tokenType();
            if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
                tokenizer.printError("Statement Let Array- Expression","let statement invalid end");
            compileExpression(); // WIP
            
            writeSymbol(); // ']'

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Statement Let Array- Symbol","No more tokens for LET_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.SYMBOL)
                tokenizer.printError("Statement Let Array- Symbol","let statement invalid end");
            writeSymbol(); // '='
        }

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement Let - Expression","No more tokens for LET_STATEMENT.");
        TokenType type = tokenizer.tokenType();
        if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
            tokenizer.printError("Statement Let - Expression","let statement invalid end");
        compileExpression(); //WIP

        if(tokenizer.tokenType() != TokenType.SYMBOL)
            tokenizer.printError("Statement Let - Symbol","let statement missing closer. ';' missing. ");
        writeSymbol(); // ;

        closeTagAndDecrementIndent("</LetStatement>");
    }

    public void compileWhile()
    {
        openTagAndIncrementIndent("<WhileStatement>");

        writeKeyword(); // While

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement While - Open Parenthesis","No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
            tokenizer.printError("Statement While - Open Parenthesis","while statement missing condition parenthesis");
        writeSymbol(); // (

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement While - Expression","No more tokens for LET_STATEMENT.");
        TokenType type = tokenizer.tokenType();
        if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
            tokenizer.printError("Statement While - Expression","let statement invalid end");
        compileExpression(); //WIP

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
            tokenizer.printError("Statement While - Close Parenthesis","while statement missing condition parenthesis");
        writeSymbol(); // )

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement While - Open Bracket","No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='{')
            tokenizer.printError("Statement While - Open Bracket","while statement missing body brace");
        writeSymbol(); // {

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement While - Keyword","No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            compileStatements();

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='}')
            tokenizer.printError("Statement While - Symbol","while statement missing body brace");
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
            tokenizer.printError("Statement Return - END","No more tokens for RETURN_STATEMENT.");

        TokenType type = tokenizer.tokenType();
        if(type == TokenType.KEYWORD || type == TokenType.IDENTIFIER || type == TokenType.INT_CONST || type == TokenType.STRING_CONST || (type == TokenType.SYMBOL && tokenizer.symbol() != ';'))
        {
            compileExpression(); //WIP
        }
        
        if( (tokenizer.tokenType() == TokenType.SYMBOL) && ( tokenizer.symbol() == ';' ) )
            writeSymbol();
        else
            tokenizer.printError("Statement Return - Closer","No more tokens for RETURN_STATEMENT.");

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
            tokenizer.printError("Statement If - Open Parenthesis","No more tokens for IF_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
            tokenizer.printError("Statement If - Open Parenthesis","while statement missing condition parenthesis");
        writeSymbol(); // (

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement If - Expressions","No more tokens for IF_STATEMENT.");
        compileExpression(); //WIP

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
            tokenizer.printError("Statement If - Close Parenthesis","while statement missing condition parenthesis");
        writeSymbol(); // )

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement If - Open Bracket","No more tokens for IF_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='{')
            tokenizer.printError("Statement If - Open Bracket","while statement missing body brace");
        writeSymbol(); // {

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement If - Statements","No more tokens for IF_STATEMENT.");
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            compileStatements(); //WIP

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='}')
            tokenizer.printError("Statement If - CloseBracket","while statement missing body brace");
        writeSymbol(); // }

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement If - END","No more tokens for IF_STATEMENT.");
        
        if(tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyword().equals("else"))
        {
            writeKeyword(); // ELSE
            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Statement If - Else Symbol","No more tokens for IF_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='{')
                tokenizer.printError("Statement If - Else Symbol","while statement missing body brace");
            writeSymbol(); // {

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Statement If - Else Statements","No more tokens for IF_STATEMENT.");
            if(tokenizer.tokenType() == TokenType.KEYWORD)
                compileStatements(); //WIP

            if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='}')
                tokenizer.printError("Statement If - Close Bracket","while statement missing body brace");
            writeSymbol(); // }

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Statement If - END","No more tokens for IF_STATEMENT.");
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
            tokenizer.printError("Subroutine Call - Symbol","No more tokens for SUBROUTINECALL_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            tokenizer.printError("Subroutine Call - Symbol","sub routine syntax error");
        writeSymbol(); // '(' or '.'

        if(tokenizer.symbol() == '.')
        {
            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Subroutine Call - Identifier","No more tokens for SUBROUTINECALL_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                tokenizer.printError("Subroutine Call - Identifier","sub routine name is missing error");
            writeIdentifier(); // NAME

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Subroutine Call - Symbol","No more tokens for SUBROUTINECALL_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
                tokenizer.printError("Subroutine Call - Symbol","sub missing condition parenthesis");
            writeSymbol(); //(
        }

        compileExpressionList();

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
            tokenizer.printError("Subroutine Call - Close Parenthesis","sub routine missing condition parenthesis");
        writeSymbol(); // )

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Subroutine Call - END","No more tokens for SUBROUTINECALL_STATEMENT.");

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
                tokenizer.printError("Expression - Auxillarity Term","No more tokens for EXPRESSION.");
            TokenType type = tokenizer.tokenType();
            if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
                tokenizer.printError("Expression - Auxillarity Term","expression op requires term");
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
                    tokenizer.printError("Term - END","No more tokens for TERM.");
                break;
            case STRING_CONST:
                writeStringConst();
                if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Term - END","No more tokens for TERM.");
                break;
            case KEYWORD:    
                if( !tokenizer.keyword().equals("true") &&  !tokenizer.keyword().equals("false") &&  !tokenizer.keyword().equals("null") && !tokenizer.keyword().equals("this"))
                    tokenizer.printError("Term - Keyword","not a keyword constant");    
                writeKeyword();
                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("Term - END","No more tokens for TERM.");
                break;
            case SYMBOL:
                TermSymbolChecks();
                break;
            default:
                tokenizer.printError("Term","not a term token");
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
                    tokenizer.printError("Term - Closing Parenthesis","not a term token");

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
                    tokenizer.printError("Term - Identifier","No more tokens for TERM.");
                if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                    tokenizer.printError("Term - Identifier","subroutine name missing.");
                writeIdentifier();

                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("Term - Symbol","No more tokens for TERM.");
                if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
                    tokenizer.printError("Term - Symbol","sub missing condition parenthesis");
                writeSymbol();
                compileExpressionList();
                
                if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
                    tokenizer.printError("Term - Symbol","sub routine missing condition parenthesis");
                writeSymbol();
                break;
            default:
                return;
        }
        
        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Term - END","No more tokens for TERM.");
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
        openTagAndIncrementIndent("<ExpressionList>");

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("ExpressionList - Type","No more tokens for EXPRESSION_LIST.");
        TokenType type = tokenizer.tokenType();
        if(type == TokenType.KEYWORD || type == TokenType.IDENTIFIER || type == TokenType.INT_CONST || type == TokenType.STRING_CONST || type == TokenType.SYMBOL )
        {
            compileExpression();

            if(tokenizer.tokenType() == TokenType.SYMBOL)
                while (tokenizer.symbol() == ',') 
                {
                    writeSymbol(); // ,
                    
                    if(!tokenizer.ifTokensAdvance())
                        tokenizer.printError("ExpressionList - Expression","No more tokens for EXPRESSION_LIST.");
                    if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST && type == TokenType.SYMBOL)
                        tokenizer.printError("ExpressionList - Expression","expresion list, opened list.");
                    compileExpression();

                    if(tokenizer.tokenType() != TokenType.SYMBOL)
                        break;
                }
        }
        
        closeTagAndDecrementIndent("</ExpressionList>");
    }

    private void compileType(boolean addVoid, String caller, String typeFor)
    {
        //Void or TYPE
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for " + caller);
        if(tokenizer.tokenType() != TokenType.KEYWORD && tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Type missing for " + typeFor);
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            {
                String key = tokenizer.keyword();
                if(key.equals("int") || key.equals("char") || key.equals("boolean") || (addVoid ? key.equals("void") : false)  )
                    writeKeyword();
                else
                    throw new RuntimeException("Type is INVALID for " + typeFor);
            }
        else
            writeIdentifier();
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
            tokenizer.printError("WRITE-LINE","Failed To write to file");
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
