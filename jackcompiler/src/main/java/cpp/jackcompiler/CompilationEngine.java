package cpp.jackcompiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine 
{

    private int indentationCount;
    private JackTokenizer tokenizer;
    private FileWriter writer;

    private SymbolTable symTable;
    private VMWriter vmWriter;

    private String className;

    private long ifIndex;
    private long whileIndex;


    //In file should be confirm as .jack outisde of engine. outfile should also be assured to not exist.
    public CompilationEngine(File inFile)
    {
        indentationCount = 0;
        ifIndex = 0;
        whileIndex = 0;
        tokenizer = new JackTokenizer(inFile);

        int lastDot = inFile.getPath().lastIndexOf('.');
        File outFile = new File( inFile.getPath().substring(0,lastDot).concat(".xml") ); // Filepath[.jack]->Filepath[.xml]
        File vmOutFile = new File( inFile.getPath().substring(0,lastDot).concat(".vm") ); // Filepath[.jack]->Filepath[.xml]
        vmWriter = new VMWriter(vmOutFile);

        try 
        {
            if(vmOutFile.exists())
                vmOutFile.delete();
            vmWriter = new VMWriter(vmOutFile);
            
            outFile.createNewFile();
            writer = new FileWriter(outFile);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }       
        symTable = new SymbolTable();

    }

    public void closeWriter()
    {
        try 
        {
            writer.close();
            vmWriter.closeVMWriter();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public void compileClass()
    {
        openTagAndIncrementIndent("<class>");

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
        className = tokenizer.identifier();
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

        closeTagAndDecrementIndent("</class>");
    }

    public void compileClassVarDec()
    {
        openTagAndIncrementIndent("<ClassVarDec>");
     
        String key = tokenizer.keyword();
        writeKeyword(); // static or field

        String type = compileType(false, "CLASS_VAR_DEC", "var");

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Class Var Dec Identifier","No more tokens for CLASS_VAR_DEC.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("Class Var Dec Identifier","Variable Name Missing");
        String identifier = tokenizer.identifier();
        writeIdentifier(); // VAR NAME

        Kind knd = key.equals("static") ? Kind.STATIC : Kind.FIELD;
        symTable.Define(identifier, type, knd);

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
            identifier = tokenizer.identifier();
            writeIdentifier(); // VAR NAME

            symTable.Define(identifier, type, knd);

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

        symTable.StartSubroutine();
        
        String subType = tokenizer.keyword();
        writeKeyword(); // Constructor Function Method

        if(subType.equals("method"))
            symTable.Define("this", className, Kind.ARG);

        compileType(true, "SUB_ROUTINE", "sub_route");
        
        //NAME
        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Class Subroutine - Identifier","No more tokens for CLASS_SUB_ROUTE.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("Class Subroutine - Identifier","Subroutine Name Missing");
        String subId =tokenizer.identifier();
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

        //VM
        switch (subType) 
        {
            case "constructor":
                vmWriter.writeFunction( className.concat(".new"), symTable.getVARCount());
                vmWriter.writePush(Segment.CONST, symTable.getFIELDCount());
                vmWriter.writeCall("Memory.alloc", 1);
                vmWriter.writePush(Segment.POINTER, 0);
                break;

            case "function":
                vmWriter.writeFunction(className.concat(".").concat(subId), symTable.getVARCount());
                break;

            case "method":
                vmWriter.writeFunction(className.concat(".").concat(subId), symTable.getVARCount());
                vmWriter.writePush(Segment.ARG, 0);
                vmWriter.writePop(Segment.POINTER, 0);
                break;
        
            default:
                break;
        }
        //

        if(tokenizer.tokenType() == TokenType.KEYWORD)
            compileStatements();

        if(tokenizer.tokenType() != TokenType.SYMBOL && tokenizer.symbol() == '}')
            tokenizer.printError("Class Subroutine - Closing Bracket","SUB ROUTINE Closing parenthesis missing.");
        writeSymbol(); // }
        
        closeTagAndDecrementIndent("</Subroutine>");
    }

    public void compileParameterList()
    {
        openTagAndIncrementIndent("<parameterList>");

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("ParameterList - Closing","No more tokens for ParamList.");
        if(tokenizer.tokenType() == TokenType.SYMBOL)
        {
            closeTagAndDecrementIndent("</parameterList>");
            return;
        }
        String type = "";
        if(tokenizer.tokenType() != TokenType.KEYWORD && tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("ParameterList - Var Type","Var Type Missing");
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            {
                String key = tokenizer.keyword();
                if(key.equals("int") | key.equals("char") | key.equals("boolean"))
                {
                    type = tokenizer.keyword();
                    writeKeyword();
                }
                else
                    tokenizer.printError("ParameterList - Var Type","Variable Type INVALID");
            }
        else
        {
            type = tokenizer.identifier();
            writeIdentifier(); //TYPE
        }
        

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("ParameterList -Identifier","No more tokens for ParamList.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("ParameterList -Identifier","Var Name Missing");
        String name = tokenizer.identifier(); 
        writeIdentifier(); //VarName
        
        symTable.Define(name, type, Kind.ARG);

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("ParameterList -Symbol","No more tokens for ParamList.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            tokenizer.printError("ParameterList -Symbol","invalid parameter list end.");

        while ( tokenizer.symbol() == ',' ) 
        {
            writeSymbol(); // ,

            type = compileType(false, "PARAM_LIST", "param");

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("ParameterList - Auxiliary identifiers","No more tokens for ParamList.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                tokenizer.printError("ParameterList - Auxiliary identifiers","Variable Name Missing");
            name = tokenizer.identifier(); 
            writeIdentifier(); //Name

            symTable.Define(name, type, Kind.ARG);
            
            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("ParameterList - symbol","No more tokens for ParamList.");
            if( (tokenizer.tokenType() != TokenType.SYMBOL))
                break;
        }

        closeTagAndDecrementIndent("</parameterList>");
    }

    public void compileVarDec()
    {
        openTagAndIncrementIndent("<varDec>");

        while (tokenizer.keyword().equals("var")) //varDec*
        {
            writeKeyword();
            
            String type = compileType(false, "VAR_DEC", "varDec");
        
            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Var Dec - Identifier ","No more tokens for VAR_DEC.");
            if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                tokenizer.printError("Var Dec - Identifier ","varDEC Name Missing");
            String identifier = tokenizer.identifier();
            writeIdentifier(); //varName

            symTable.Define(identifier, type, Kind.VAR);

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
                identifier = tokenizer.identifier();
                writeIdentifier(); // VarName

                symTable.Define(identifier, type, Kind.VAR);

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

        closeTagAndDecrementIndent("</varDec>");
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
        openTagAndIncrementIndent("<doStatement>");

        writeKeyword(); // Do

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement Do - Call Identifier","No more tokens for DO_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("Statement Do - Call Identifier","do statement subroutine/var/class name missing");
        compileSubroutineCall();

        vmWriter.writePop(Segment.TEMP, 0);

        if(tokenizer.tokenType() != TokenType.SYMBOL)
            tokenizer.printError("Statement Do - Symbol","do statement closer missing.");
        writeSymbol(); // ;

        closeTagAndDecrementIndent("</doStatement>");
    }

    public void compileLet()
    {
        openTagAndIncrementIndent("<letStatement>");

        writeKeyword(); // Let

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement Let- Identifier","No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.IDENTIFIER)
            tokenizer.printError("Statement Let- Identifier","let statement variable name Missing");
        String var = tokenizer.identifier();
        Kind knd = symTable.KindOf(var);
        int index = symTable.IndexOf(var);
        boolean arrayAccess = false;
        writeIdentifier(); // varName

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Statement Let- Symbol","No more tokens for LET_STATEMENT.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            tokenizer.printError("Statement Let- Symbol","let statement invalid end");
        writeSymbol(); // '=' or '['
        
        if(tokenizer.symbol() == '[')
        {
            arrayAccess = true;

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Statement Let Array- Expression","No more tokens for LET_STATEMENT.");
            TokenType type = tokenizer.tokenType();
            if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
                tokenizer.printError("Statement Let Array- Expression","let statement invalid end");
            compileExpression(); // WIP
            
            writeSymbol(); // ']'
            
            vmWriter.writePush(kindToSegment(knd), index);
            vmWriter.writeArithmetic('+');
            vmWriter.writePop(Segment.TEMP, 0);

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

        if(arrayAccess)
        {
            vmWriter.writePush(Segment.TEMP, 0);
            vmWriter.writePop(Segment.POINTER, 1);
            vmWriter.writePop(Segment.THAT, 0);
        }
        else
        {
            vmWriter.writePop(kindToSegment(knd), index);
        }

        if(tokenizer.tokenType() != TokenType.SYMBOL)
            tokenizer.printError("Statement Let - Symbol","let statement missing closer. ';' missing. ");
        writeSymbol(); // ;

        closeTagAndDecrementIndent("</letStatement>");
    }

    public void compileWhile()
    {
        openTagAndIncrementIndent("<whileStatement>");

        long ActiveIndex = whileIndex++;
        vmWriter.writeLabel("WHILE_".concat(String.valueOf(ActiveIndex)));

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

        vmWriter.writeArithmetic('$'); //NOT
        vmWriter.writeIf("WHILE_END_".concat(String.valueOf(ActiveIndex))); //GOTO END IF WHILE IS NOT FUFILLED

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

        vmWriter.writeGoto("WHILE_".concat(String.valueOf(ActiveIndex))); //Return TO While Check
        vmWriter.writeLabel("WHILE_END_".concat(String.valueOf(ActiveIndex)));

        closeTagAndDecrementIndent("</whileStatement>");
    }

    public void compileReturn()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<returnStatement>");
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
        else
        {
            vmWriter.writePush(Segment.CONST, 0);
        }
        
        vmWriter.writeReturn();

        if( (tokenizer.tokenType() == TokenType.SYMBOL) && ( tokenizer.symbol() == ';' ) )
            writeSymbol();
        else
            tokenizer.printError("Statement Return - Closer","No more tokens for RETURN_STATEMENT.");

        sb = new StringBuilder();
        indentationCount--;
        sb.append("\t".repeat(indentationCount));
        sb.append("</returnStatement>");
        writeLine(sb.toString());
    }

    public void compileIf()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<ifStatement>");
        indentationCount++;
        writeLine(sb.toString());

        long ActiveIndex = ifIndex++;
        
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

        //VM
        vmWriter.writeIf("IF_TRUE_".concat(String.valueOf(ActiveIndex)));
        vmWriter.writeGoto("IF_FALSE_".concat(String.valueOf(ActiveIndex))); //Skiped Over If True
        vmWriter.writeLabel("IF_TRUE_".concat(String.valueOf(ActiveIndex)));
        //

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
        
        //VM
        vmWriter.writeGoto("IF_END_".concat(String.valueOf(ActiveIndex))); // Skip Else Statement
        vmWriter.writeLabel("IF_FALSE_".concat(String.valueOf(ActiveIndex))); // Will Be Joined With IF_END if no else block exists
        //

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

        vmWriter.writeLabel("IF_END_".concat(String.valueOf(ActiveIndex)));

        sb = new StringBuilder();
        indentationCount--;
        sb.append("\t".repeat(indentationCount));
        sb.append("</ifStatement>");
        writeLine(sb.toString());
    }

    private void compileSubroutineCall()
    { 
        String function = "";
        String id = tokenizer.identifier();
        String type = symTable.TypeOf(id);
        Kind knd = symTable.KindOf(id);
        Integer index = symTable.IndexOf(id);
        int agrsCount = 0;

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
            
            //VM
            if(type != null)
            {
                agrsCount += 1;
                vmWriter.writePush(kindToSegment(knd), index);
                function = type;
            }
            else
            {
                function = id;
            }
            function = function.concat(".").concat(tokenizer.identifier());
            //

            writeIdentifier(); // NAME

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Subroutine Call - Symbol","No more tokens for SUBROUTINECALL_STATEMENT.");
            if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
                tokenizer.printError("Subroutine Call - Symbol","sub missing condition parenthesis");
            writeSymbol(); //(
        }
        else
        {
            agrsCount++;
            vmWriter.writePush(Segment.POINTER, 0);
        }

        agrsCount += compileExpressionList();

        if(function.isEmpty())
        {
            vmWriter.writeCall(className.concat(".").concat(id), agrsCount);
        }
        else
        {
            vmWriter.writeCall(function, agrsCount);
        }

        if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !=')')
            tokenizer.printError("Subroutine Call - Close Parenthesis","sub routine missing condition parenthesis");
        writeSymbol(); // )
        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Subroutine Call - END","No more tokens for SUBROUTINECALL_STATEMENT.");
    }

    public void compileExpression()
    {
        boolean empty = true;
        openTagAndIncrementIndent("<expression>");

        compileTerm();

        while(tokenizer.tokenType() == TokenType.SYMBOL && isOP(tokenizer.symbol()) )
        {
            //VM
            vmWriter.writeArithmetic(tokenizer.symbol());
            //

            writeSymbol();

            if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Expression - Auxillarity Term","No more tokens for EXPRESSION.");
            TokenType type = tokenizer.tokenType();
            if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST)
                tokenizer.printError("Expression - Auxillarity Term","expression op requires term");
            compileTerm();
        }
        
        closeTagAndDecrementIndent("</expression>");
    }

    public void compileTerm()
    {
        openTagAndIncrementIndent("<term>");

        switch (tokenizer.tokenType()) 
        {
            case IDENTIFIER:
                TermIdentifierChecks();
                break;
            case INT_CONST: 
                //VM
                vmWriter.writePush(Segment.CONST, tokenizer.intVal());
                //
                writeIntegerConst();
                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("Term - END","No more tokens for TERM.");
                break;
            case STRING_CONST:
                //VM
                String str = tokenizer.stringVal();
                int start = str.indexOf('"');
                int last = str.lastIndexOf('"');
                str = str.substring( start+1 , last);
                vmWriter.writePush(Segment.CONST, str.length());
                vmWriter.writeCall("String.new", 1);
                for (int i=0; i<str.length(); i++) 
                {
                    vmWriter.writePush(Segment.CONST,str.codePointAt(i) );
                    vmWriter.writeCall("String.appendChar", 2); 
                }
                //
                writeStringConst();
                if(!tokenizer.ifTokensAdvance())
                tokenizer.printError("Term - END","No more tokens for TERM.");
                break;
            case KEYWORD:    
                if( !tokenizer.keyword().equals("true") &&  !tokenizer.keyword().equals("false") &&  !tokenizer.keyword().equals("null") && !tokenizer.keyword().equals("this"))
                    tokenizer.printError("Term - Keyword","not a keyword constant");    
                
                switch (tokenizer.keyword()) {
                    case "null":
                    case "false":
                        vmWriter.writePush(Segment.CONST, 0);
                        break;
                    
                    case "true":
                        vmWriter.writePush(Segment.CONST, 1);
                        vmWriter.writeArithmetic('$');
                        break;

                    case "this":
                        vmWriter.writePush(Segment.POINTER, 0);
                        break;
                    default:
                        break;
                }

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

        closeTagAndDecrementIndent("</term>");

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
                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("'term sym","No more tokens for term.");
                compileTerm();
                writeSymbol();
                //VM
                vmWriter.writeArithmetic('$');
                //
                break;
            case '~':
                writeSymbol();
                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("'term sym","No more tokens for term.");
                compileTerm();
                //VM
                vmWriter.writeArithmetic('~');
                //
                break;
        
            default: // pass check up
        }
    }

    private void TermIdentifierChecks()
    {
        //VM
        int agrsCount = -1;
        String id = tokenizer.identifier();
        String type = symTable.TypeOf( id );
        int index = symTable.IndexOf( id );
        Kind knd = symTable.KindOf( id );
        String modifiers = "<" + type + "/>" + "<" + index + "/>" + "<" + knd + "/>"  ;
        //
        writeIdentifier(modifiers);

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("Term - Identifier","No more tokens for TERM.");
        if(tokenizer.tokenType() != TokenType.SYMBOL)
        {
            //VM
            vmWriter.writePush(kindToSegment(knd), index);
            //
            return;
        }
        
        switch (tokenizer.symbol()) {
            case '[': // VARNAME[expressions]
                writeSymbol();
                //VM
                    vmWriter.writePush(kindToSegment(knd), index);
                //
                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("Term - Identifier Array","No more tokens for TERM.");
                compileExpression();
                //VM
                    vmWriter.writeArithmetic('+');
                    vmWriter.writePop(Segment.POINTER, 1);
                    vmWriter.writePush(Segment.THAT, 0);
                //
                writeSymbol(); // ]
                break;

            case '(': //Subroutine Call
                writeSymbol();
                
                //VM
                agrsCount = 1;
                vmWriter.writePush(Segment.POINTER, 0);
                //

                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("Term - Identifier Subroutine Call","No more tokens for TERM.");
                //VM
                agrsCount += compileExpressionList();
                vmWriter.writeCall(className.concat(id), agrsCount);
                //

                writeSymbol();
                break;

            case '.': //Subroutine Call
                writeSymbol();
                agrsCount = 0;
                if(type != null)
                {
                    agrsCount++;
                    vmWriter.writePush(kindToSegment(knd), index);
                }

                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("Term - Identifier","No more tokens for TERM.");
                if(tokenizer.tokenType() != TokenType.IDENTIFIER)
                    tokenizer.printError("Term - Identifier","subroutine name missing.");
                String subName = tokenizer.identifier();
                writeIdentifier();

                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("Term - Symbol","No more tokens for TERM.");
                if(tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() !='(')
                    tokenizer.printError("Term - Symbol","sub missing condition parenthesis");
                writeSymbol();

                agrsCount += compileExpressionList();
                
                //  VM
                if(type != null)
                {
                    vmWriter.writeCall(type.concat(subName), agrsCount); // Type.SubName
                }
                else
                {   
                    vmWriter.writeCall(id.concat(subName), agrsCount); // Class.SubName
                }
                //

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

    private int compileExpressionList()
    {
        int count = 0;
        openTagAndIncrementIndent("<expressionList>");

        if(!tokenizer.ifTokensAdvance())
            tokenizer.printError("ExpressionList - Type","No more tokens for EXPRESSION_LIST.");
        
        if(tokenizer.tokenType() == TokenType.SYMBOL && (tokenizer.symbol() == ')') ) 
            count--;

        compileExpression();
        count++;

        TokenType type = TokenType.UNSET;
        if(tokenizer.tokenType() == TokenType.SYMBOL)
            while (tokenizer.symbol() == ',') 
            {
                writeSymbol(); // ,
                count++;
                
                if(!tokenizer.ifTokensAdvance())
                    tokenizer.printError("ExpressionList - Expression","No more tokens for EXPRESSION_LIST.");
                if(type != TokenType.KEYWORD && type != TokenType.IDENTIFIER && type != TokenType.INT_CONST && type != TokenType.STRING_CONST && type == TokenType.SYMBOL)
                    tokenizer.printError("ExpressionList - Expression","expresion list, opened list.");
                compileExpression();

                if(tokenizer.tokenType() != TokenType.SYMBOL)
                    break;
            }
        
        
        closeTagAndDecrementIndent("</expressionList>");
        return count;
    }

    private String compileType(boolean addVoid, String caller, String typeFor)
    {
        String type;
        //Void or TYPE
        if(!tokenizer.ifTokensAdvance())
            throw new RuntimeException("No more tokens for " + caller);
        if(tokenizer.tokenType() != TokenType.KEYWORD && tokenizer.tokenType() != TokenType.IDENTIFIER)
            throw new RuntimeException("Type missing for " + typeFor);
        if(tokenizer.tokenType() == TokenType.KEYWORD)
            {
                String key = tokenizer.keyword();
                if(key.equals("int") || key.equals("char") || key.equals("boolean") || (addVoid ? key.equals("void") : false)  )
                {
                    type = tokenizer.keyword();
                    writeKeyword();
                }   
                else
                    throw new RuntimeException("Type is INVALID for " + typeFor);
            }
        else
        {
            type = tokenizer.identifier();
            writeIdentifier();
        }
        return type;
    }

    private void writeKeyword()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<keyword>");

        sb.append(tokenizer.keyword());

        sb.append("</keyword>");
        writeLine(sb.toString());
    }

    private void writeSymbol()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<symbol> ");

        sb.append(tokenizer.symbol());

        sb.append(" </symbol>");
        writeLine(sb.toString());
    }

    private void writeIdentifier(String mods)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<identifier>\n");

        sb.append("\t".repeat(indentationCount+1));
        sb.append(""+mods+"\n");

        sb.append("\t".repeat(indentationCount+2));
        sb.append(tokenizer.identifier() + "\n");

        sb.append("\t".repeat(indentationCount+1));
        sb.append(""+mods+"\n");

        sb.append("\t".repeat(indentationCount));
        sb.append(" </identifier>");
        writeLine(sb.toString());
    }

    private void writeIdentifier()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<identifier> ");
        sb.append(tokenizer.identifier());
        sb.append(" </identifier>");
        writeLine(sb.toString());
    }

    private void writeStringConst()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<stringConstant> ");

        sb.append(tokenizer.stringVal());

        sb.append(" </stringConstant>");
        writeLine(sb.toString());
    }

    private void writeIntegerConst()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indentationCount));
        sb.append("<integerConstant> ");

        sb.append( String.valueOf( tokenizer.intVal() ) );

        sb.append(" </integerConstant>");
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
        writeLine(sb.toString());
        indentationCount++;
    }

    private void closeTagAndDecrementIndent(String tag)
    {
        StringBuilder sb = new StringBuilder();
        indentationCount--;
        sb.append("\t".repeat(indentationCount));
        sb.append(tag);
        writeLine(sb.toString());
    }

    private Segment kindToSegment(Kind knd)
    {
        switch (knd) {
            case ARG:
                return Segment.ARG;
            case FIELD:
                return Segment.THIS;//
            case STATIC:
                return Segment.TEMP;
            case VAR:
                return Segment.LOCAL;

            case NULL:
                break;
        }
        return null;
    }

}
