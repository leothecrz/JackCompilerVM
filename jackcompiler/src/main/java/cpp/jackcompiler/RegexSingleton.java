package cpp.jackcompiler;

import java.util.regex.Pattern;

public class RegexSingleton 
{
    private static final RegexSingleton instance = new RegexSingleton();
    private Pattern keywords;
    private Pattern symbols;
    private Pattern identifier;
    private Pattern stringPattern;
    private Pattern intPattern;
    private Pattern comments;

    // private constructor to avoid client applications using the constructor
    private RegexSingleton()
    {
         keywords = Pattern.compile("(class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return)");
         symbols = Pattern.compile("([{}()\\[\\].,;+-*/&=<|>-])");
         identifier = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
         stringPattern = Pattern.compile("\"(.)*\"");
         intPattern = Pattern.compile("\\d+");
         comments = Pattern.compile("(//.*)|(/\\*([^*]|[\r\n]|(\\*+([^*/]|[\r\n])))*\\*+/)");
    }

    public static RegexSingleton getInstance() 
    {
        return instance;
    }

    public Pattern getComments() {
        return comments;
    }
    public Pattern getIdentifier() {
        return identifier;
    }
    public Pattern getIntPattern() {
        return intPattern;
    }
    public Pattern getKeywords() {
        return keywords;
    }
    public Pattern getStringPattern() {
        return stringPattern;
    }
    public Pattern getSymbols() {
        return symbols;
    }

}
