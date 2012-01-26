import oscP5.*;
import netP5.*;

Track[] track = new Track[6];
SceneSwitch scenes;

void setup() {
    size( 775, 300 );
    smooth();
    textFont( createFont( "DejaVu Sans", 8 ));

    for( int i = 0; i < track.length; ++i ) {
        track[i] = new Track( 25 + ( i * 125 ), 75, i );
    }
    
    scenes = new SceneSwitch( 25, 25 );
}


void draw() {
    background( 70 );

    for( int i = 0; i < track.length; ++i ) {
        track[i].render();
    }
    
    scenes.render();
}

void mouseReleased() {
    for( int i = 0; i < track.length; ++i ) {
        track[i].click();
    }
    scenes.click();    
}

void mouseDragged() {
    for( int i = 0; i < track.length; ++i ) {
        track[i].drag();
    }
}

void keyReleased() {
    println( frameRate );
}
