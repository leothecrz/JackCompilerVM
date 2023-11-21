package cpp.jackcompiler;

import java.util.HashMap;

public class SymbolTable 
{
    private HashMap<String, SymbolData> classScopeTable;
    private HashMap<String, SymbolData> subroutineScopeTable;
    private HashMap<Kind, Integer> kindCounts;

    static class SymbolData
    {
        String type;
        Kind kind;
        int index;
        
        public SymbolData(String tp, Kind knd, int indx)
        {
            this.type = tp;
            this.kind = knd;
            this.index = indx;
        }
    }

    public SymbolTable()
    {
        classScopeTable = new HashMap<>();
        subroutineScopeTable = new HashMap<>();
        
        kindCounts = new HashMap<>();
        kindCounts.put(Kind.STATIC, 0);
        kindCounts.put(Kind.FIELD, 0);
        kindCounts.put(Kind.ARG, 0);
        kindCounts.put(Kind.VAR, 0);
    }

    public void StartSubroutine()
    {
        subroutineScopeTable.clear();
    }

    public void Define(String name, String type, Kind knd)
    {
        switch (knd) 
        {
            case STATIC:
            case FIELD:
                classScopeTable.put(name, new SymbolData(type, knd, kindCounts.get(knd)));
                break;

            case ARG:
            case VAR:
                subroutineScopeTable.put(name, new SymbolData(type, knd, kindCounts.get(knd)));
                break;
            default: throw new RuntimeException("Unset Identifier Type");
        }

        kindCounts.put(knd, kindCounts.get(knd) + 1);

    }

    public int VarCount(Kind knd)
    {
        return kindCounts.get(knd);
    }

    public Kind KindOf(String name)
    {
        if(subroutineScopeTable.containsKey(name))
            return subroutineScopeTable.get(name).kind;
        else if(classScopeTable.containsKey(name))
            return classScopeTable.get(name).kind;
        else
            return Kind.NULL;
    }

    public String TypeOf(String name)
    {
        if(subroutineScopeTable.containsKey(name))
            return subroutineScopeTable.get(name).type;
        else if(classScopeTable.containsKey(name))
            return classScopeTable.get(name).type;
        else
            return null;
    }

    public int IndexOf(String name)
    {
        if(subroutineScopeTable.containsKey(name))
            return subroutineScopeTable.get(name).index;
        else if(classScopeTable.containsKey(name))
            return classScopeTable.get(name).index;
        else
            return -1;
    }

}
