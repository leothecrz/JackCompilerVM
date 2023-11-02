package cpp.jackcompiler;

import java.io.File;

public class JackAnalyzer 
{


    public static void main(String[] args) 
    {
        JackTokenizer tokenizer = new JackTokenizer(new File("\\Users\\Repo\\Java\\MyJackCompiller\\JackCompilerVM\\jackcompiler\\test\\test.jack"));
        
        while (tokenizer.hasMoreTokens()) 
        {
            tokenizer.advance();
            tokenizer.printState();
        }
    }

    // public static void test()
    // {
    //     RegexSingleton re = RegexSingleton.getInstance();

    //     String test = "";
    //     while (!test.isEmpty()) 
    //     {
    //         for(int i=0; i<re.getPatternsLength(); i++)
    //         {
    //             Matcher mat = re.getPatterns()[i].matcher(test);
    //             if(mat.find() && (mat.start()==0))
    //             {
    //                 System.out.println(i);
    //                 System.out.println(test);
    //                 test = mat.replaceFirst("");
    //                 System.out.println(test);
    //                 i = re.getPatternsLength();
    //             }
    //         }
    //     }
    // }
    
}
