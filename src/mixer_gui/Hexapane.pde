class Hexapane {

    public Hexapane( int ux, int uy ) {
        x = ux;
        y = uy;
        a = 100;

        hit_area = new Button( ux, uy, a );
        dot = new Dot( ux, uy, a, a );

        speakers = new Speaker[6];
        // speaker size
        int sa = 8;
        int sh = sa / 2;
        // left series of speakers from bottom to top;
        speakers[0] = new Speaker( x, y );
        speakers[1] = new Speaker( x, y+(a/2)-sh );
        speakers[2] = new Speaker( x, y+a-sa );
        // rigat speakerseries of speakers from bottom to top;
        speakers[3] = new Speaker( x+a-sa, y );
        speakers[4] = new Speaker( x+a-sa, y+(a/2)-sh );
        speakers[5] = new Speaker( x+a-sa, y+a-sa );
    }

    public void click() {
        for( Speaker s : speakers ) {
            s.mute();
        }
    }
    
    public void drag() {
        if( hit_area.is_hit() ) {
            dot.move();
            // update dot's distance to each speaker
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
        for( int i = 0; i < speakers.length; ++i ) {
            speakers[i].render();
        }
    }
    
    private Dot dot;
    private Speaker[] speakers;
    private Button hit_area;
    private int x, y, a;
}


