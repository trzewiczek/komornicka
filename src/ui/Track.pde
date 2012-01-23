class Track {
  public Track( int ux, int uy, int inx ) {
    area = new Rectangle( ux, uy, 100, 100 );
    panel  = new Hexapane( ux, uy + 25 );
    number = inx;
    mute   = new Toggle( "TRACK " + inx, ux, uy, 100, 15 );

    inputs = new Toggle[6];
    for( int i = 0; i < inputs.length; ++i ) {
      inputs[i] = new Toggle( i+"", ux + ( i * 17 ), uy + 140, 15, 15 );
    }
  }
  
  public void render() {
    pushStyle();
    
    fill( 150 );
    panel.render();
    mute.render();
    for( int i = 0; i < inputs.length; ++i ) {
      inputs[i].render();
    }    
    
    popStyle();
  }
  
  public void click() {
    panel.click();
    mute.click();
    for( int i = 0; i < inputs.length; ++i ) {
      inputs[i].click();
    }    
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
