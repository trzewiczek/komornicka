class SceneSwitch {
  public SceneSwitch( int ux, int uy ) {
    x = ux;
    y = uy;

    area = new Rectangle( ux, uy, width-50, 15 );
    offset = 18;
    
    Toggle first_scene = new Toggle( "1", x+35, y );
    first_scene.toggle();
    
    scenes = new ArrayList<Toggle>( 25 );    
    scenes.add( first_scene );
    
    new_scene = new Toggle( "NOWA SCENA", x + 50 + ( scenes.size() * offset ), y, 75, 15 );
  }

  public void render() {
    pushStyle();
    
    fill( 120 );
    text( "SCENY", x, y+10 );
    
    for( Toggle t : scenes ) {
      t.render();
    }
    
    stroke( 120 );
    int lx = x + 42 + ( scenes.size() * offset );
    line( lx, y, lx, y+15 );

    new_scene.render();
    
    popStyle();
  }
  
  public void click() {
    if( new_scene.click() ) {
      String inx = "" + (scenes.size() + 1);
 
      scenes.add( new Toggle( inx, x+35+(scenes.size() * offset), y ) );
      new_scene = new Toggle( "NOWA SCENA", x + 50 + ( scenes.size() * offset ), y, 75, 15 );
    }
    else if( area.contains( mouseX, mouseY ) ) {
      for( Toggle s : scenes ) {
        s.off();
        s.click();
      }
    }
  }
  
  private ArrayList<Toggle> scenes;
  private int x, y;
  private Toggle new_scene;
  private int offset;
  private Rectangle area;
}
