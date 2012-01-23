import java.awt.Rectangle;
import java.awt.Point;

class Dot {

    public Dot( int ux, int uy, int uw, int uh ) {
        area = new Rectangle( ux, uy, uw, uh );
        head = new Point( (int)area.getCenterX(), (int)area.getCenterY() );
    }


    public void move() {
        if( area.contains( mouseX, mouseY ) ) {
            head.setLocation( mouseX, mouseY );
        }
    }


    public Point get_position() {
        return head.getLocation();
    }


    public float get_distance( Point speaker ) {
      return MAX_FLOAT;
    }


    public void render() {
        pushMatrix();
        pushStyle();

        translate( (float)head.getX(), (float)head.getY() );
        noStroke();
        fill( 100, 150, 50 );

        ellipse( 0, 0, 6, 6 );

        popStyle();
        popMatrix();
    }


    private Rectangle area;
    private Point     head;
}
