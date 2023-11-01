package cpp.jackcompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;

public class JackTokenizer 
{
    
    private RegexSingleton re;

    private BufferedReader reader;
    private Queue<StackToken> tokenStack;
    private String activeline;

    private String activeKeyword;
    private int activeInt;
    private char activeSym;
    private TokenType activeTokenType;

    public JackTokenizer(File inputFile)
    {
        try 
        {
            reader = new BufferedReader(new FileReader(inputFile));
        } 
        catch (FileNotFoundException e) 
        {
            System.err.println("Failed To Initiate File Reader. File Could Not Be Found");
            e.printStackTrace();
        }
        tokenStack = new LinkedList<>();
        activeline = "";
        activeKeyword = "";
        activeInt = Integer.MAX_VALUE;
        activeSym = 0;
        activeTokenType = TokenType.UNSET;
        re = RegexSingleton.getInstance();
    }

    public void advanced()
    {
        if(tokenStack.isEmpty())
        {
            if(!hasMoreTokens())
            {
                System.err.println("NO MORE TOKENS");
                return;
            }
        }

        StackToken tkn = tokenStack.poll();

        switch (tkn.getType()) {
            case KEYWORD: 
                activeKeyword = tkn.getData();
                activeTokenType = TokenType.KEYWORD;
                break;

            case SYMBOL: 
                activeSym = tkn.getData().charAt(0);
                activeTokenType = TokenType.SYMBOL;
                break;

            case IDENTIFIER: 
                activeKeyword = tkn.getData();
                activeTokenType = TokenType.IDENTIFIER;
                break;

            case INT_CONST: 
                activeInt = Integer.parseInt( tkn.getData() );
                activeTokenType = TokenType.INT_CONST;
                break;

            case STRING_CONST: 
                activeKeyword = tkn.getData();
                activeTokenType = TokenType.STRING_CONST;
                break;
            
            case UNSET:
                break;
            default:
                System.err.println("tkn failure state");
                break;
        }

            
    }

    public void printState()
    {
        System.out.println(this.activeKeyword);
        System.out.println(this.activeInt);
        System.out.println(this.activeSym);
        System.out.println(this.tokenType());
    }

    public boolean hasMoreTokens()
    {
        if(!tokenStack.isEmpty())
            return true;
        try 
        {
            if( (activeline = reader.readLine()) != null )
            { 
                tokenizeString(activeline);
                return true;
            }
        } 
        catch (IOException e) 
        {
            System.err.println("Line Could Not Be READ.");
            e.printStackTrace();
        }

        return false;
    }

    public void tokenizeString(String str)
    {
        String copy = new String(str);
        Matcher mat;
        while (!copy.isEmpty()) 
        {

            mat = re.getWhitespace().matcher(copy);
            if(mat.find() && mat.start()==0)
            {
                copy = mat.replaceFirst("");
            }

            mat = re.getComments().matcher(copy);
            if(mat.find() && mat.start()==0)
            {
                copy = mat.replaceFirst("");
            }

            mat = re.getSymbols().matcher(copy);
            if(mat.find() && mat.start()==0)
            {
                tokenStack.add(new StackToken(TokenType.SYMBOL, mat.group()));
                copy = mat.replaceFirst("");
                continue;
            }

            mat = re.getKeywords().matcher(copy);
            if(mat.find() && mat.start()==0)
            {
                tokenStack.add(new StackToken(TokenType.KEYWORD, mat.group()));
                copy = mat.replaceFirst("");
                continue;
            }

            mat = re.getIdentifier().matcher(copy);
            if(mat.find() && mat.start()==0)
            {
                tokenStack.add(new StackToken(TokenType.IDENTIFIER, mat.group()));
                copy = mat.replaceFirst("");
                continue;
            }

            mat = re.getIntPattern().matcher(copy);
            if(mat.find() && mat.start()==0)
            {
                tokenStack.add(new StackToken(TokenType.INT_CONST, mat.group()));
                copy = mat.replaceFirst("");
                continue;
            }

            mat = re.getStringPattern().matcher(copy);
            if(mat.find() && mat.start()==0)
            {
                tokenStack.add(new StackToken(TokenType.STRING_CONST, mat.group()));
                copy = mat.replaceFirst("");
                continue;
            }

        }

    }


    public TokenType tokenType()
    {
        return activeTokenType;
    }

    public String keyword()
    {
        return activeKeyword;
    }

    public char symbol()
    {
        return activeSym;
    }

    public String identifier()
    {
        return activeKeyword;
    }

    public int intVal()
    {
        return activeInt;
    }

    public String stringVal()
    {
        return activeKeyword;
    }
    
    private class StackToken
    {
        private TokenType type;
        private String data;

        public StackToken(TokenType tp, String dta)
        {
            type = tp;
            data = dta;
        }

        public TokenType getType()
        {
            return type;
        }

        public String getData()
        {
            return data;
        }
 
    }
    
}
