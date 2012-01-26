class Hexapane {

  public Hexapane( int ux, int uy, int utrack ) {
    x = ux;
    y = uy;
    a = 100;
    track = utrack;

    hit_area = new Button( ux, uy, a );
    dot = new Dot( ux, uy, a, a );

    speakers = new Speaker[6];
    // speaker size
    int sa = 8;
    int sh = sa / 2;
    // speakers (l, r) from bottom to top;
    speakers[0] = new Speaker( x, y );
    speakers[1] = new Speaker( x+a-sa, y );
    speakers[2] = new Speaker( x, y+(a/2)-sh );
    speakers[3] = new Speaker( x+a-sa, y+(a/2)-sh );
    speakers[4] = new Speaker( x, y+a-sa );
    speakers[5] = new Speaker( x+a-sa, y+a-sa );
  }

  public void click() {
    if ( hit_area.is_hit() ) {
      for ( int i = 0; i < speakers.length; ++i ) {
        String osc = "/track/"+track+"/speaker/" + i + "/mute/";
        osc += speakers[i].mute();
        println( osc );
      }
    }
  }

  public void drag() {
    if ( hit_area.is_hit() ) {
      dot.move();
      // TODO update dot's distance to each speaker
      // TODO send it via OSC
      println( "/track/"+track+"/head/" + dot.get_position() );
    }
  }      

  public void render() {
    pushMatrix();
    pushStyle();
    translate( x, y );

    fill( 80 );
    stroke( 120 );
    rect( 0, 0, a, a );
    line( 0, 0, a, a );
    line( 0, a, a, 0 );
    line( 0, a / 2, a, a / 2 );
    stroke( 100 );
    line( a / 2, 0, a / 2, a );

    fill( 120 );
    textAlign( CENTER );
    text( "SCENA", a/2, 10 );

    popStyle();
    popMatrix();

    dot.render();
    for ( int i = 0; i < speakers.length; ++i ) {
      speakers[i].render();
    }
  }

  private Dot dot;
  private Speaker[] speakers;
  private Button hit_area;
  private int x, y, a;
  private int track;
}

