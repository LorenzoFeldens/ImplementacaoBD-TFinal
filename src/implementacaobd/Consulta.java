package implementacaobd;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;

public class Consulta {    
    private ArrayList<ArrayList<String>> colsSelect;
    private ArrayList<ArrayList<String>> colsWhere;
    private ArrayList<ArrayList<String>> colsJoin;
    private ArrayList<ArrayList<String>> tableWheres;
    private ArrayList<String> whereAfter;
    private ArrayList<String> whereBefore;
    private ArrayList<String> colunas;
    private ArrayList<String> tabelas;
    private ArrayList<String> joins;
    private ArrayList<String> wheres;
    private ArrayList<String> parenteses;
    private ArrayList<Integer> abreP;
    private ArrayList<Integer> fechaP;
    private Arvore arvOriginal;
    private Arvore arvOtimizada;
    private String consulta;
    private String select;
    private String from;
    private String where;
    
    public Consulta(String consulta){
        this.consulta = consulta.trim().toUpperCase();
        
        colunas = new ArrayList();
        tabelas = new ArrayList();
        joins = new ArrayList();
        parenteses = new ArrayList();
        wheres = new ArrayList();
        abreP = new ArrayList();
        fechaP = new ArrayList();
        arvOriginal = null;
        arvOtimizada = null;
        colsWhere = new ArrayList<>();
        colsSelect = new ArrayList<>();
        colsJoin = new ArrayList<>();
        whereBefore = new ArrayList<>();
        whereAfter = new ArrayList<>();
        tableWheres = new ArrayList();
    }
    
    private String separaConsulta(){
        String[] consulta_v = this.consulta.split(" ");
        if(!consulta_v[0].equalsIgnoreCase("SELECT"))
            return "Consulta não inicia com SELECT";
        
        if(!consulta.contains(" FROM "))
            return "Clausula 'FROM' não encontrada";
        
        String[] s0 = consulta.split("SELECT ");
        String[] s1 = s0[1].split(" FROM ");
        
        select = s1[0];
        
        if(!s1[1].contains(" WHERE ")){
            from = s1[1].replace("(", "").replace(")", "");
            if(s1[1].contains(" WHERE")){
                return "Clausula WHERE incompleta";
            }
        }else{
            String[] s2 = s1[1].split(" WHERE ");
            if(s2.length != 2){
                return "Clausula WHERE incompleta";
            }
            from = s2[0].replace("(", "").replace(")", "");
            where = s2[1];
        }
        return "OK";
    }
    
    private String verificaOn(String str, int x){
        String[] s0 = str.split(" = ");
        if(s0.length!=2){
            return "Erro na clausula ON\n Em: "+str; 
        }

        for(int i=0; i<s0.length; i++){
            if(s0[i].length() - s0[i].replace(".", "").length() != 1){
                return "Erro nas colunas da clausula ON\n Em: "+s0[i];
            }
            
            String[] s1 = s0[i].split("[.]");
            if(s1.length != 2){
                return "Erro nas colunas da clausula ON\n Em: "+s0[i];
            }
            
            int tab = 0;
            for(int j=0; j<x+2; j++){
                if(s1[0].equals(tabelas.get(j))){
                    tab = 1;
                }
            }
            if(tab==0){
                return "Tabela da clausula ON não encontrada...";
            }
        }
        
        return "OK";
    }
    
    private String verificaJoins(){
        for(int i=0; i<joins.size(); i++){
            if(!joins.get(i).contains(" = ")){
                    return "Sem ' = ' no ON do JOIN\n Em: "+joins.get(i);
            }
            
            String[] s0 = joins.get(i).split(" AND ");
            for(int j=0; j<s0.length; j++){
                String ret = verificaOn(s0[j],i);
                if(!ret.equalsIgnoreCase("OK")){
                    return ret;
                }                
            }
        }
        
        return "OK";
    }
    
    private String verificaFrom(){
        if(!from.contains(" JOIN ")){
            tabelas.add(from.trim());
        }else{
            if(!from.contains(" ON ")){
                return "Clausula 'ON' não encontrada";
            }
            
            String[] s0 = from.split(" JOIN ");
            tabelas.add(s0[0].trim());
            
            for(int i=1; i<s0.length; i++){
                String[] s1 = s0[i].split(" ON ");
                tabelas.add(s1[0].trim());
            }
        }
        
        String[] s0 = from.split(" JOIN ");
        for(int i=0; i<s0.length-1; i++){
            if(!s0[i+1].contains(" ON "))
                return "Clausula ON não encontrada";
            
            String[] s1 = s0[i+1].split(" ON ");
            if(s1.length!=2)
                return "Erro na cláusula ON";
            joins.add(s1[1]);
        }
        
        String ret = verificaJoins();
        if(!ret.equalsIgnoreCase("OK")){
            return ret;
        }
        
        return "OK";
    }
    
    private String verificaSelect(){
        String[] s0 = select.split(",");
        for(int i=0; i<s0.length; i++){
            String[] s1 = s0[i].split("[.]");
            if(s1.length<=1 || s1[1].replace(" ","").equalsIgnoreCase("")){
                return "Coluna inválida na clausula SELECT\n Em: "+s0[i];
            }
            
            if(!tabelas.contains(s1[0].replace(" ", ""))){
                return "Clausula SELECT possui coluna não presente na clausula"
                        + " FROM\n Em: "+s1[0];
            }
            colunas.add(s0[i].trim());
        }
        
        for(int i=0; i<colunas.size(); i++){
            if((colunas.get(i).length() - colunas.get(i)
                    .replace(".", "").length()) > 1){
                return "Coluna inválida na clausula "
                        + "SELECT\n Em: "+colunas.get(i);
            }
            if(colunas.get(i).trim().contains(" ")){
                return "Coluna inválida na clausula "
                        + "SELECT\n Em: "+colunas.get(i);
            }
        }
        
        return "OK";
    }
    
    private String verificaWhere(){
        if(where!=null){
            if(where.contains("(")){
                int np = 0;
                for(int i=0; i<where.length(); i++){
                    if(where.substring(i, i+1).equals("(")){
                        abreP.add(i);
                        np++;
                    }
                    if(where.substring(i, i+1).equals(")")){
                        np--;
                        fechaP.add(i);
                        if(np<0){
                            return "Erro no fechamento dos parênteses";
                        }
                    }
                }
                if(np!=0)
                    return "Erro na utilização do parênteses";
                if(abreP.size()!=fechaP.size())
                    return "Erro na utilização dos parênteses";
                String s = where;
                int i=0;
                while(!abreP.isEmpty()){
                    String ss = s.substring(abreP.get(i)+1,fechaP.get(0));
                    if(!ss.contains("(")){
                        parenteses.add(ss);
                        int dif = s.length();
                        s = s.replaceFirst("\\("+ss+"\\)", " @"
                                +String.valueOf(parenteses.size()-1)+"@ ");
                        dif-=s.length();
                        
                        for(int j=1; j<fechaP.size(); j++){
                            int x;
                            if(j>i){
                                x = abreP.get(j);
                                x-=dif;
                                abreP.set(j, x);
                            }
                            x = fechaP.get(j);
                            x-=dif;
                            fechaP.set(j, x); 
                        }
                        abreP.remove(i);
                        fechaP.remove(0);
                        i=0;
                    }else
                        i++;
                }
                parenteses.add(s);
            }else{
                if(where.contains(")"))
                    return "Erro na abertura dos parênteses";
                parenteses.add(where);
                String[] s0 = where.split(" AND | OR ");
                for(int x=0; x<s0.length; x++){
                    if(!s0[x].contains("@"))
                        wheres.add(s0[x]);
                }
            }
        }
        
        for(int i=0; i<parenteses.size(); i++){
            String[] s = parenteses.get(i).split(" AND | OR ");
            for(int j=0; j<s.length; j++){
                if(!s[j].contains("@"))
                    wheres.add(s[j]);
                else{
                    String[] sss = s[j].trim().split("@");
                    if(sss.length != 2)
                        return "Erro na clausula WHERE";
                }
            }
        }
        for(int i=0; i<wheres.size(); i++){
            if(!(wheres.get(i).contains(" > ") || wheres.get(i).contains(" < ") 
                    || wheres.get(i).contains(" = ")))
                return "Erro nos operadores na cláusula WHERE\n Em: "
                        +wheres.get(i);
            String[] s0 = wheres.get(i).split(" > | < | = ");
            if(s0.length != 2)
                return "Erro nos operadores na cláusula WHERE\n Em: "
                        +wheres.get(i);
            if(!(s0[0].trim().startsWith("\"") || s0[0].trim().startsWith("'") 
                    || s0[1].trim().startsWith("\"") || s0[1].trim()
                            .startsWith("'")))
                if(s0[0].trim().contains(" ") || s0[1].trim().contains(" "))
                    return "Erro na clausula WHERE\n Em: "+wheres.get(i);
            if(!(s0[0].contains(".") || s0[1].contains(".")))
                return "Erro nas tabelas presentes na cláusula WHERE\n Em: "
                        +wheres.get(i);
            
            int tab = 0;
            if(s0[0].contains("'")){
                String ls = s0[0].replace("'","");
                if(s0[0].length()-ls.length()!=2){
                    return "Erro nos operadores na claúsula WHERE\n Em: "+s0[0];
                }
            }else{
                if(s0[0].contains("\"")){
                    String ls = s0[0].replace("\"","");
                    if(s0[0].length()-ls.length()!=2){
                        return "Erro nos operadores na claúsula WHERE\n Em: "
                                +s0[0];
                    }
                }else{
                    if(s0[0].contains(".")){
                        String ls = s0[0].replace(".","");
                        if(s0[0].length()-ls.length()!=1){
                            return "Erro nos operadores na claúsula WHERE\n "
                                    + "Em: "+s0[0];
                        }
                        
                        String[] s1 = s0[0].split("[.]");
                        if(s1.length!=2){
                            return "Erro nos operadores na claúsula WHERE\n "
                                    + "Em: "+s0[0];
                        }
                        
                        if(!s1[0].matches("[0-9]+")){
                            if(!tabelas.contains(s1[0].trim())){
                                return "Erro nas tabelas presentes na cláusula "
                                        + "WHERE\n Em: "+s1[0];
                            }else{
                                tab = 1;
                            }  
                        }else{
                            if(!s1[1].matches("[0-9]+")){
                                return "Erro nos operadores na claúsula WHERE\n"
                                        + " Em: "+s1[1];
                            }
                        }
                    }else{
                        if(!s0[0].matches("[0-9]+")){
                            return "Erro nos operadores na claúsula WHERE\n"
                                    + " Em: "+s0[1];
                        }
                    }
                }
            }
            
            if(s0[1].contains("'")){
                String ls = s0[1].replace("'","");
                if(s0[1].length()-ls.length()!=2){
                    return "Erro nos operadores na claúsula WHERE\n Em: "+s0[1];
                }
            }else{
                if(s0[1].contains("\"")){
                    String ls = s0[1].replace("\"","");
                    if(s0[1].length()-ls.length()!=2){
                        return "Erro nos operadores na claúsula WHERE\n"
                                + " Em: "+s0[1];
                    }
                }else{
                    if(s0[1].contains(".")){
                        String ls = s0[1].replace(".","");
                        if(s0[1].length()-ls.length()!=1){
                            return "Erro nos operadores na claúsula WHERE\n"
                                    + " Em: "+s0[1];
                        }
                        
                        String[] s1 = s0[1].split("[.]");
                        if(s1.length!=2){
                            return "Erro nos operadores na claúsula WHERE\n"
                                    + " Em: "+s0[1];
                        }
                        
                        if(!s1[0].matches("[0-9]+")){
                            if(!tabelas.contains(s1[0].trim())){
                                return "Erro nas tabelas presentes na cláusula"
                                        + " WHERE\n Em: "+s1[0];
                            }else{
                                tab = 1;
                            }
                        }else{
                            if(!s1[1].matches("[0-9]+")){
                                return "Erro nos operadores na claúsula WHERE\n"
                                        + " Em: "+s1[1];
                            }
                        }
                    }else{
                        if(!s0[1].matches("[0-9]+")){
                            return "Erro nos operadores na claúsula WHERE\n"
                                    + " Em: "+s0[1];
                        }
                    }
                }
            }
            
            if(tab == 0){
                return "Erro nas tabelas presentes na cláusula WHERE\n Em: "
                        +wheres.get(i);
            }
        }
        
        return "OK";
    }
    
    public String verificaErros(){
        if(consulta.equalsIgnoreCase("")){
            return "Consulta vazia!";
        }
        
        String ret = separaConsulta();
        if(!ret.equalsIgnoreCase("OK")){
            return ret;
        }
        
        ret = verificaFrom();
        if(!ret.equalsIgnoreCase("OK")){
            return ret;
        }
        
        ret = verificaSelect();
        if(!ret.equalsIgnoreCase("OK")){
            return ret;
        }
        
        ret = verificaWhere();
        if(!ret.equalsIgnoreCase("OK")){
            return ret;
        }
        
        return "OK";
    }
    
    public void geraArvoreOriginal(){
        if(tabelas.size() == 1){
            arvOriginal = new Arvore("FROM",tabelas.get(0));
        }else{
            for(int i=0; i<joins.size(); i++){
                Arvore j = arvOriginal;
                arvOriginal = new Arvore("JOIN",joins.get(i));
                Arvore a = new Arvore("FROM",tabelas.get(i+1));
                
                if(i==0){
                    Arvore b = new Arvore("FROM",tabelas.get(0));
                    arvOriginal.addFilho(b,a);
                }else{
                    arvOriginal.addFilho(j,a);
                }
            }
        }
        
        Arvore j = arvOriginal;
        if(!wheres.isEmpty()){
            arvOriginal = new Arvore("WHERE", where);
            arvOriginal.addFilho(j);
            j = arvOriginal;
        }
        
        arvOriginal = new Arvore("SELECT", select);
        arvOriginal.addFilho(j);
        setDadosArvore(arvOriginal);
    }
    
    private void setDadosArvore(Arvore a){
        int altura = alturaArvore(a);
        setMargemArvore(a,getDistArvore(a));
        setAlturaArvore(a, 0);
        setNivelArvore(a, altura);
    }
    
    private void setAlturaArvore(Arvore a, int alt){
        if(a == null)
            return;
        a.setAlt(alt);
        setAlturaArvore(a.getEsq(), alt+1);
        setAlturaArvore(a.getDir(), alt+1);
    }
    
    private void setMargemArvore(Arvore a, int dd){
        if(a == null)
            return;
        a.setMargem(dd);
        if(a.getDir() == null)
            setMargemArvore(a.getEsq(), dd);
        else{
            setMargemArvore(a.getEsq(), dd-1);
            setMargemArvore(a.getDir(), dd+1);
        }
    }
    
    private void setNivelArvore(Arvore a, int alt){
        int dd = 0;
        for(int i=0; i<alt; i++)
            dd = setNivelAltura(a,i,dd);
        
    }
    
    private int setNivelAltura(Arvore a, int alt, int dd){
        if(a==null)
            return dd;
        if(a.getAlt()==alt){
            a.setNivel(dd);
            return dd+1;
        }
        int r = setNivelAltura(a.getEsq(), alt, dd);
        return setNivelAltura(a.getDir(), alt, r);
    }
    
    private int getDistArvore(Arvore a){
        if(a == null){
            return 0;
        }
        
        return a.getDist()+getDistArvore(a.getEsq());
    }
    
    private int alturaArvore(Arvore a){
        if(a == null){
            return 0;
        }
        
        int x = alturaArvore(a.getEsq());
        int y = alturaArvore(a.getDir());
        if(x>y){
            return x+1;
        }else{
            return y+1;
        }
    }
    
    public void geraArvoreOtimizada(){
        for(int i=0; i<tabelas.size(); i++){
            colsSelect.add(new ArrayList<>());
            colsWhere.add(new ArrayList<>());
            colsJoin.add(new ArrayList<>());
            tableWheres.add(new ArrayList<>());
        }
        
        for(int i=0; i<colunas.size(); i++){
            String[] s = colunas.get(i).trim().split("[.]");
            for(int j=0; j<tabelas.size(); j++){
                if(s[0].equalsIgnoreCase(tabelas.get(j))){
                    colsSelect.get(j).add(colunas.get(i));
                }
            }
        }
        
        for(int i=0; i<joins.size(); i++){
            String[] s = joins.get(i).trim().split(" = ");
            String[] ss1 = s[0].trim().split("[.]");
            String[] ss2 = s[1].trim().split("[.]");
            
            for(int j=0; j<tabelas.size(); j++){
                if(ss1[0].equalsIgnoreCase(tabelas.get(j))){
                    colsSelect.get(j).add(s[0]);
                }
                if(ss2[0].equalsIgnoreCase(tabelas.get(j))){
                    colsSelect.get(j).add(s[1]);
                }
            }
        }
        
        if(!parenteses.isEmpty()){
            verificaParentese(parenteses.get(parenteses.size()-1));
        }   
        
        for(int i=0; i<whereBefore.size(); i++){
            for(int j=0; j<tabelas.size(); j++){
                if(whereBefore.get(i).contains(tabelas.get(j)+".")){
                    tableWheres.get(j).add(whereBefore.get(i));
                    getColunasWhere(whereBefore.get(i),j);
                }
            }
        }
                
        if(tabelas.size() == 1){
            arvOtimizada = getBranchTable(0);
        }else{
            for(int i=0; i<joins.size(); i++){
                Arvore j = arvOtimizada;
                arvOtimizada = new Arvore("JOIN",joins.get(i));
                Arvore a = getBranchTable(i+1);
                
                if(i==0){
                    Arvore b = getBranchTable(0);
                    arvOtimizada.addFilho(b,a);
                }else{
                    arvOtimizada.addFilho(j,a);
                }
            }
        }
        
        String whe = "";
        for(int i=0; i<whereAfter.size(); i++){
            if(!whe.equals("")){
                whe+=" AND ";
            }
            whe += whereAfter.get(i);
        }
        
        if(!whe.equalsIgnoreCase("")){
            Arvore j = arvOtimizada;
            arvOtimizada = new Arvore("WHERE", whe);
            arvOtimizada.addFilho(j);
        }
        
        Arvore k = arvOtimizada;
        arvOtimizada = new Arvore("SELECT", select);
        arvOtimizada.addFilho(k);
        
        setDadosArvore(arvOtimizada);
    }
    
    private void getColunasWhere(String str, int x){
        String[] s = str.split(" OR ");
        
        for(int i=0; i<s.length; i++){
            String[] s0 = s[i].split(" > | < | = ");
            if(!(s0[0].contains("'") || s0[0].contains("\""))){
                if(!s0[0].replace("[.]", "").matches("[0-9]+")){
                    colsWhere.get(x).add(s0[0]);
                }
            }
            if(!(s0[1].contains("'") || s0[1].contains("\""))){
                if(!s0[1].replace("[.]", "").matches("[0-9]+")){
                    colsWhere.get(x).add(s0[1]);
                }
            }
        }
    }
    
    private Arvore getBranchTable(int x){
        Arvore a = new Arvore("FROM",tabelas.get(x));
        
        Set<String> hs = new HashSet<>();
        hs.addAll(colsSelect.get(x));
        hs.addAll(colsJoin.get(x));
        hs.addAll(colsWhere.get(x));
        colsSelect.get(x).clear();
        colsSelect.get(x).addAll(hs);
        
        String sel = "";
        for(int i=0; i<colsSelect.get(x).size(); i++){
            if(!sel.equalsIgnoreCase("")){
                sel+=", ";
            }
            sel += colsSelect.get(x).get(i);
        }
        
        if(!sel.equalsIgnoreCase("")){
            Arvore b = a;
            a = new Arvore("SELECT",sel);
            a.addFilho(b);
        }
        
        String whe = "";
        for(int i=0; i<tableWheres.get(x).size(); i++){
            if(!whe.equalsIgnoreCase("")){
                whe+=" AND ";
            }
            whe+=tableWheres.get(x).get(i);
        }
        
        if(!whe.equalsIgnoreCase("")){
            Arvore b = a;
            a = new Arvore("WHERE",whe);
            a.addFilho(b);
        }
        
        return a;
    }
    
    private void verificaParentese(String p){
        String[] s = p.trim().split(" AND ");
        for(int i=0; i<s.length; i++){
            verificaAnd(s[i]);
        }
    }
    
    private void verificaAnd(String p){
        if(!p.contains(" OR ")){
            if(p.contains("@")){
                verificaParentese(parenteses.get(Integer.valueOf(p
                        .replace("@", "").trim())));
            }else{
                String tab1 = "";
                String tab2 = "";
                
                String[] s = p.split(" > | < | = ");
                
                if(!(s[0].contains("'") || s[0].contains("\""))){
                    if(!s[0].replace("[.]","").matches("[0-9]+")){
                        String[] s0 = s[0].split("[.]");
                        tab1 = s0[0];
                        for(int i=0; i<tabelas.size(); i++){
                            if(s0[0].equalsIgnoreCase(tabelas.get(i))){
                                colsWhere.get(i).add(s[0]);
                            }
                        }
                    }
                }
                
                if(!(s[1].contains("'") || s[1].contains("\""))){
                    if(!s[1].replace("[.]","").matches("[0-9]+")){
                        String[] s1 = s[1].split("[.]");
                        tab2 = s1[0];
                        for(int i=0; i<tabelas.size(); i++){
                            if(s1[0].equalsIgnoreCase(tabelas.get(i))){
                                colsWhere.get(i).add(s[1]);
                            }
                        }
                    }
                }
                
                if(!tab1.equalsIgnoreCase("")){
                    if(tab2.equalsIgnoreCase("")){
                        whereBefore.add(p);
                    }else{
                        if(tab1.equalsIgnoreCase(tab2)){
                            whereBefore.add(p);
                        }else{
                            whereAfter.add(p);
                        }
                    }
                }
            }
        }else{
            verificaOr(p);
        }
    }
    
    private void verificaOr(String p){
        String[] s = p.split(" OR ");
        
        ArrayList<String> first = verificaLadoOr(s[0]);
        
        Set<String> hs = new HashSet<>();
        hs.addAll(first);
        first.clear();
        first.addAll(hs);
        
        if(first.size()>1){
            whereAfter.add(p);
            return;
        }
        
        for(int i=1; i<s.length; i++){
            ArrayList<String> next = verificaLadoOr(s[1]);
            hs = new HashSet<>();
            hs.addAll(next);
            next.clear();
            next.addAll(hs);
            
            if(next.size()>1){
                whereAfter.add(p);
                return;
            }
            if(first.get(0).equals(next.get(0))){
                whereBefore.add(p);
                return;
            }
        }
        whereAfter.add(p);
    }
    
    private ArrayList verificaLadoOr(String p){
        ArrayList<String> ret = new ArrayList();
        
        String[] s = p.split("@");
        String f = "";
        for(int i=0; i<s.length; i++){
            if(s[i].matches("[0-9]+")){
                f+=" "+parenteses.get(Integer.valueOf(s[i].replace("@", "")
                        .trim()))+" ";
            }else{
                f+=s[i]+" ";
            }
        }
        
        for(int i=0; i<tabelas.size(); i++){
            if(f.trim().contains(tabelas.get(i)+".")){
                ret.add(tabelas.get(i));
            }
        }
        
        String[] s0 = f.split(" AND | OR ");
        for(int i=0; i<s0.length; i++){
            String[] s1 = s0[i].split(" > | < | = ");
            for(int j=0; j<s1.length; j++){
                if(!(s1[i].contains("'") || s1[i].contains("\""))){
                    if(!s1[i].replace(".", "").matches("[0-9]+")){
                        String[] s2 = s1[i].split("[.]");
                        for(int k=0; k<tabelas.size(); k++){
                            if(s2[0].equalsIgnoreCase(tabelas.get(k))){
                                colsWhere.get(k).add(s1[i]);
                            }
                        }
                    }
                }
            }
        }
        
        return ret;
    }
    
    public void mostraArvoreOriginal(){
        PainelArvore p = new PainelArvore(arvOriginal);
        p.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        p.setSize(dimension);
        p.setExtendedState(p.getExtendedState()|JFrame.MAXIMIZED_BOTH);
        p.setVisible(true);
    }
    
    public void mostraArvoreOtimizada(){
        PainelArvore p = new PainelArvore(arvOtimizada);
        p.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        p.setSize(dimension);
        p.setExtendedState(p.getExtendedState()|JFrame.MAXIMIZED_BOTH);
        p.setVisible(true);
    }
}
