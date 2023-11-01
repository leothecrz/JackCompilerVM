package cpp.jackcompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
import java.util.regex.Pattern;

public class JackTokenizer 
{
    
    private RegexSingleton re;

    private BufferedReader reader;
    private Stack<StackToken> tokenStack;
    private String activeline;

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
        tokenStack = new Stack<>();
        activeline = "";
        re = RegexSingleton.getInstance();
    }

    public void advanced()
    {

    }

    public boolean hasMoreTokens()
    {
        if(!tokenStack.empty())
            return true;
        try 
        {
            if( (activeline = reader.readLine()) != null )
            {
                //
                // READ LINE NEEDS TO BE SPLIT INTO TOKENS
                //
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

    public TokenType tokenType()
    {
        return null;
    }

    public String  keyword()
    {
        return null;
    }

    public char symbol()
    {
        return '-';
    }

    public String identifier()
    {
        return "";
    }

    public int intVal()
    {
        return 0;
    }

    public String stringVal()
    {
        return null;
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
