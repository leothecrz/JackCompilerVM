package cpp.jackcompiler;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class VMWriter 
{
    private File outFile;
    private FileWriter writer;
    
    public VMWriter(File outputfile)
    {
        outFile = outputfile;
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

    public String SegmentString(Segment seg)
    {
        switch (seg) 
        {
            case ARG:
                return "argument";
            case CONST:
                return "constant";
            case LOCAL:
                return "local";
            case POINTER:
                return "pointer";
            case STATIC:
                return "static";
            case TEMP:
                return "temp";
            case THAT:
                return "that";
            case THIS:
                return "this";
            default:
                return null;
        }
    }

    public void writePush(Segment seg, int Index)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("push ");
        sb.append(SegmentString(seg));
        sb.append(" ");
        sb.append(String.valueOf(Index));
        sb.append("\n");
        try 
        {
            writer.write(sb.toString());
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

    }

    public void writePop(Segment seg, int Index)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("pop ");
        sb.append(SegmentString(seg));
        sb.append(" ");
        sb.append(String.valueOf(Index));
        sb.append("\n");
        try 
        {
            writer.write(sb.toString());
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

    }

    public void writeArithmetic(Character c)
    {
        StringBuilder sb = new StringBuilder();
        
        switch (c) 
        {
            case '+':
                sb.append("add");
                break;
            case '-':
                sb.append("sub");
                break;
            case '*':
                sb.append("call Math.multiply 2");
                break;
            case '/':
                sb.append("call Math.divide 2");
                break;
            case '&':
                sb.append("and");
                break;
            case '|':
                sb.append("or");
                break;
            case '<':
                sb.append("lt");
                break;
            case '>':
                sb.append("gt");
                break;
            case '=':
                sb.append("eq");
                break;

            //Unary operators
            case '~':
                sb.append("neg");
                break;
            case '$':
                sb.append("not");
                break;
            default:
                break;
        }

        sb.append("\n");
        try 
        {
            writer.write(sb.toString());
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

    }

    public void writeLabel(String lbl)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("label ");
        sb.append(lbl);
        sb.append("\n");
        try 
        {
            writer.write(sb.toString());
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public void writeGoto(String lbl)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("goto ");
        sb.append(lbl);
        sb.append("\n");
        try 
        {
            writer.write(sb.toString());
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public void writeIf(String lbl)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("if-goto ");
        sb.append(lbl);
        sb.append("\n");
        try 
        {
            writer.write(sb.toString());
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public void writeCall(String name, int argsCount)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("call ");
        sb.append(name);
        sb.append(" ");
        sb.append(String.valueOf(argsCount));
        sb.append("\n");
        try 
        {
            writer.write(sb.toString());
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public void writeFunction(String name, int localsCount)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("function ");
        sb.append(name);
        sb.append(" ");
        sb.append(String.valueOf(localsCount));
        sb.append("\n");
        try 
        {
            writer.write(sb.toString());
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public void writeReturn()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("return\n");
        try 
        {
            writer.write(sb.toString());
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public void closeVMWriter()
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
    
}
