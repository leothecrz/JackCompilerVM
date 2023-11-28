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
    private Pattern whitespace;

    private RegexSingleton()
    {
        keywords = Pattern.compile("^\\s*(class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return)");
        symbols = Pattern.compile("^\\s*([~{}()\\[\\].,;\\+\\-\\*\\/&=<\\|>-])"); //SYM
        identifier = Pattern.compile("^\\s*([a-zA-Z_][a-zA-Z0-9_]*)"); // Starts with letter
        stringPattern = Pattern.compile("^\\s*\"(.)*\""); // Quoted
        intPattern = Pattern.compile("^\\s*\\d+"); //Digit
        comments = Pattern.compile("(?:^(?:\\/\\*\\*?)(.*)(?:\\*\\/?))|(?:^\\*(.*)\\*\\/?)|(?:^\\/\\/(.*))");
        whitespace = Pattern.compile("\\s+");
    }
    public static RegexSingleton getInstance() 
    {
        return instance;
    }

    // /**
    //  * keywords; 0
    //  * symbols; 1 
    //  * identifier; 2
    //  * stringPattern; 3
    //  * intPattern; 4
    //  * comments; 5
    //  * whitespace; 6
    //  */

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
    public Pattern getWhitespace() {
        return whitespace;
    }

    
}
