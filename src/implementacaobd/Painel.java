package implementacaobd;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
 
public class Painel extends JPanel{
    private Arvore arv;
    
    public Painel(Arvore arv){
        this.arv = arv;
    }
    
    public void desenhar(){
        paint(getGraphics());
    }
 
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        Graphics2D g2d = (Graphics2D) g;
        drawArvore(arv, g2d, 0);
        drawArvore(arv, g2d, 1);
    }
    
    private void drawArvore(Arvore a, Graphics2D g2d, int t){
        if(a == null){
            return;
        }
        if(t==1){
            g2d.setStroke(new BasicStroke(2f));
            g2d.setColor(Color.black);

            g2d.setFont(new Font("default",Font.PLAIN, 30));
            g2d.drawString(a.getOperador(),60*a.getMargem()+30,
                    50*a.getNivel()+100);

            g2d.setFont(new Font("default", Font.BOLD, 15));
            g2d.drawString(a.getTexto(),60*a.getMargem()+70,
                    50*a.getNivel()+100);
        }else{
            g2d.setStroke(new BasicStroke(2f));
            g2d.setColor(Color.red);

            if(a.getDir() != null){
                g2d.drawLine(60*a.getMargem()+45,50*a.getNivel()+110,60*
                        a.getDir().getMargem()+45,50*a.getDir().getNivel()+70);
            }
            if(a.getEsq() != null){
                g2d.drawLine(60*a.getMargem()+45,50*a.getNivel()+110,60*
                        a.getEsq().getMargem()+45,50*a.getEsq().getNivel()+70);
            }
        }
        
        drawArvore(a.getEsq(), g2d, t);
        drawArvore(a.getDir(), g2d, t);
    }
}
