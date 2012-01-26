class Speaker extends Button {

    public Speaker( int ux, int uy ) {
        super( ux, uy, 8 );

        c = color( 150, 100, 50 );
        mute = false;
    }

    public boolean mute() {
        if( this.is_hit() ) {
            mute = !mute;
        }
        return mute;
    }

    public boolean is_mute() {
        return mute;
    }

    public void render() {
        pushStyle();
        
        noStroke();
        fill( !mute ? c : 120 );
        rect( x, y, a, a );

        popStyle();
    }

    public Point get_position() {
        return new Point( x, y );
    }
    
    private color c;
    private boolean mute;
}
        

