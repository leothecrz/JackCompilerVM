package cpp.jackcompiler;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class JackAnalyzer 
{
    
    private static Scanner in;

    public static void main(String[] args) 
    {
        // End if no args given
        if(args.length < 1)
        {
            return;
        }
        File inputFile = new File(args[0]);
        if(inputFile.isDirectory())
        {   
            File[] files = inputFile.listFiles();
            for (File file : files) 
            {

                //Restric To '.jack files'
                int lastDot = file.getPath().lastIndexOf('.');
                if( !file.getPath().substring(lastDot).contains("jack") )
                    continue;

                File output = new File( file.getPath().substring(0,lastDot).concat(".xml"));
                if(output.exists())
                {
                    in = new Scanner(System.in);
                    System.out.println("Output file already exist. Replace it? (n/y)");
                    char charin = in.nextLine().trim().charAt(0);
                    if( charin != 'y' && charin != 'Y' )
                        System.exit(0);
                    output.delete();
                }
                
                try {
                    output.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create outputfile");
                }
                
                CompilationEngine CE = new CompilationEngine(file);
                CE.compileClass();
                CE.closeWriter();
            }
        }
        else
        {

            //Restric To '.jack files'
            int lastDot = inputFile.getPath().lastIndexOf('.');
            if( !inputFile.getPath().substring(lastDot).contains("jack") )  
            {
                System.out.println("Not a '.jack' file");
                return;
            }

            File output = new File( inputFile.getPath().substring(0,lastDot).concat(".xml") );
            if(output.exists())
            {
                in = new Scanner(System.in);
                System.out.println("Output file already exist. Replace it? (n/y)");
                char charin = in.nextLine().trim().charAt(0);
                if( charin != 'y' && charin != 'Y' )
                    System.exit(0);
                output.delete();
            }
            
            try {
                System.out.println(output.getName());
                output.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create outputfile");
            }
            
            CompilationEngine CE = new CompilationEngine(inputFile);
            CE.compileClass();
            CE.closeWriter();
        }
    }
}
