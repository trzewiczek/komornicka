class Toggle {
  
  public Toggle( String ulabel, int ux, int uy ) {
    label = ulabel;
    area  = new Rectangle( ux, uy, 10, 10 );
    on = false;
  }
  
  public Toggle( String ulabel, int ux, int uy, int uw, int uh ) {
    label = ulabel;
    area  = new Rectangle( ux, uy, uw, uh );    
    on = false;
  }
  
  public boolean toggle() {
    on = !on;
    return on;
  }
  
  public void render() {
    pushStyle();
    
    noStroke();
    fill( 120, on ? 255 : 100 );
    rect( (float)area.getX(), (float)area.getY(), (float)area.getWidth(), (float)area.getHeight(), 4, 4 );
    
    fill( on ? 180 : 130 );
    textAlign( CENTER ); 
    text( label, (float)area.getCenterX(), (float)(area.getMaxY() - area.getHeight()/3.0));
        
    popStyle();
  }
  
  public boolean click() {
    if( area.contains( mouseX, mouseY ) ) {
      toggle();
    }
    
    return on;
  }
  
  private String    label;
  private Rectangle area;
  private boolean   on;
}
