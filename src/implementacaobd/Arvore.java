package implementacaobd;

public class Arvore {
    private String operador;
    private String texto;
    private Arvore dir;
    private Arvore esq;
    
    private int dist;
    private int alt;
    private int nivel;
    private int margem;
    
    public Arvore(String op, String texto){
        this.texto = texto;
        switch(op){
            case "SELECT":
                operador = " P";
            break;
            case "FROM":
                operador = texto;
                this.texto = "";
            break;
            case "JOIN":
                operador = "|X|";
            break;            
            case "WHERE":
                operador = " S";
            break;
        }
        esq = null;
        dir = null;
        dist=0;
    }
    
    public void addFilho(Arvore a){
        //a.incDist();
        esq = a;
    }
    public void addFilho(Arvore a, Arvore b){
        a.incDist();
        esq = a;
        dir = b;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Arvore getDir() {
        return dir;
    }

    public void setDir(Arvore dir) {
        this.dir = dir;
    }

    public Arvore getEsq() {
        return esq;
    }

    public void setEsq(Arvore esq) {
        this.esq = esq;
    }
    
    public int getDist(){
        return dist;
    }
    
    public void incDist(){
        dist++;
    }    

    public int getAlt() {
        return alt;
    }

    public void setAlt(int alt) {
        this.alt = alt;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public int getMargem() {
        return margem;
    }

    public void setMargem(int margem) {
        this.margem = margem;
    }
    
    
}
