package OpenGLFeatures;

import java.awt.*;
import java.awt.event.*;
public class TextureExample extends Frame
{ 	
  /**
	 * 
	 */
	private static final long serialVersionUID = 7972910119333847348L;
public TextureExample()
  { 
    Canvas cvas = new DrawCanvas();
    cvas.setBackground(Color.cyan);
    add("Center", cvas); 
    
    setTitle("Draw As You Like Please");
    setSize(400, 450);
    setVisible(true);
  }
  public static void main(String args[])
  {
     new  TextureExample();
  }
}
class DrawCanvas extends Canvas implements MouseListener, MouseMotionListener
{ 
  final int CIRCLESIZE = 20; 		     // becomes circle radius
  private Point lineBegin = new Point(0, 0); // point where line starts
  public DrawCanvas()
  { 
    addMouseListener(this);
    addMouseMotionListener(this);
  }  
                                             // override all the five abstract methods of ML
   public void mouseClicked(MouseEvent e)  {  }
   public void mouseEntered(MouseEvent e)  {  }
   public void mouseExited(MouseEvent e)   {  }
   public void mouseReleased(MouseEvent e) {  }

   public void mousePressed(MouseEvent e)
   {   
     if (e.isMetaDown())		     // for right mouse button
         setForeground(getBackground());     // match foreground to 
     else 			             // background(for erasing affect)
	setForeground(Color.black);          //  set foreground for drawing
                                             //get the new start end of the line
        lineBegin.move(e.getX(), e.getY());  // place in comments for different affect
   }
                                             // override methods of MML
   public void mouseDragged(MouseEvent e)
   {  		
     Graphics g = getGraphics() ;   		
     if (e.isMetaDown())                     // erase existing graphics using an oval
          g.fillOval(e.getX() - (CIRCLESIZE/2), e.getY() - (CIRCLESIZE/2), CIRCLESIZE, CIRCLESIZE);
     else
	  g.drawLine(lineBegin.x, lineBegin.y, e.getX(), e.getY());

     lineBegin.move(e.getX(), e.getY()); 
   }                                         // place the above line in comments, you will see different output
   public void mouseMoved (MouseEvent e)   {  }
}