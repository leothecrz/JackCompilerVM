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

    }

    public void compileClassVarDec()
    {

    }

    public void compileSubroutine()
    {

    }

    public void compileParameterList()
    {

    }

    public void compileVarDec()
    {

    }

    public void compileStatements()
    {

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

                switch (tokenizer.symbol()) 
                {
                    case '(':
                        writeSymbol();
                        if(!tokenizer.hasMoreTokens())
                        {

                        }
                        compileExpression();
                        tokenizer.advance();
                        writeSymbol();
                        break;
                    
                    case '-':
                    case '~':
                    writeSymbol();
                    if(!tokenizer.hasMoreTokens())
                    {

                    }
                    tokenizer.advance();
                    compileTerm();
                    break;
                }
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

    private void TermIdentifierChecks()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() != TokenType.SYMBOL)
            return;

        switch (tokenizer.symbol()) {
            case '[':
                writeSymbol();
                if(!tokenizer.hasMoreTokens())
                {
                    System.err.println("TERM ARRAY ACCESS FAILURE");
                }
                compileExpression();
                tokenizer.advance();
                writeSymbol();
                break;
            case '(':   
                writeSymbol();
                if(!tokenizer.hasMoreTokens())
                {
                    System.err.println("TERM ARRAY ACCESS FAILURE");
                }
                compileExpression();
                tokenizer.advance();
                writeSymbol();
                break;

            case '.':
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

        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(')
        {
            compileExpression();

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

        sb.append(tokenizer.keyword());

        sb.append(" </Identifier>\n");
        writeLine(sb.toString());
    }

    private void writeStringConst()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<StringConst> ");

        sb.append(tokenizer.keyword());

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
