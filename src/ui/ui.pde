Track[] track = new Track[6];

void setup() {
    size( 775, 300 );
    smooth();
    textFont( createFont( "DejaVu Sans", 8 ));

    for( int i = 0; i < track.length; ++i ) {
        track[i] = new Track( 25 + ( i * 125 ), 25, i );
    }
}


void draw() {
    background( 70 );

    for( int i = 0; i < track.length; ++i ) {
        track[i].render();
    }
}

void mouseReleased() {
    for( int i = 0; i < track.length; ++i ) {
        track[i].click();
    }
}

void mouseDragged() {
    for( int i = 0; i < track.length; ++i ) {
        track[i].drag();
    }
}

void keyReleased() {
    println( frameRate );
}
