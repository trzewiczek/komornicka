class Track {
  public Track( int ux, int uy, int inx ) {
    area = new Rectangle( ux, uy, 100, 100 );
    panel  = new Hexapane( ux, uy + 25, inx );
    number = inx;
    mute   = new Toggle( "TRACK " + inx, ux, uy, 100, 15 );

    inputs = new Toggle[6];
    for( int i = 0; i < inputs.length; ++i ) {
      inputs[i] = new Toggle( i+"", ux + ( i * 17 ), uy + 140 );
    }
  }
  
  public void render() {
    panel.render();
    mute.render();
    for( int i = 0; i < inputs.length; ++i ) {
      inputs[i].render();
    }    
  }
  
  public void click() {
    String osc = "/track/" + number + "/mute/";
    
    panel.click();
    osc += mute.click();
    osc += "/inputs/";
    for( int i = 0; i < inputs.length; ++i ) {
      if( inputs[i].click() ) {
        osc += i+",";
      }
    }    
    if( osc.endsWith(",") ) {
      osc = osc.substring( 0, osc.length()-1 );
    }
    println( osc );
  }
  
  public void drag() {
    panel.drag();
  }
  
  private Rectangle area;
  private Toggle mute;
  private int number;
  private Hexapane panel;
  private Toggle[] inputs;
}
