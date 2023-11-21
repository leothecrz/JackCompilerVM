package cpp.jackcompiler;

import java.io.File;
import java.io.IOException;

public class VMWriter 
{
    private File outFile;
    
    public VMWriter(File outputfile)
    {
        outFile = outputfile;
        
        try 
        {
            outFile.createNewFile();
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        

    }

    public void writePush(Segment seg, int Index)
    {

    }

    public void writePop(Segment seg, int Index)
    {

    }

    public void writeArithmetic(Command cmd)
    {

    }

    public void writeLabel(String lbl)
    {

    }

    public void writeGoto(String lbl)
    {

    }

    public void writeIf(String lbl)
    {

    }

    public void writeCall(String name, int argsCount)
    {

    }

    public void writeFunction(String name, int localsCount)
    {

    }

    public void writeReturn()
    {

    }

    public void closeVMWriter()
    {

    }
    
}
