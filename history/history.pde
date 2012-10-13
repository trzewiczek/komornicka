import processing.video.*;
import ddf.minim.*;

// camera object
Capture cam;
// size of the frame
int numPixels;
// previous frame buffer
float[] previousFrame;
// timer preventing triggering more jukebox with a single gesture
int last_trigger = 0;

Minim minim;
// collection of jukebox to be triggered
ArrayList<HistorySample> jukebox = new ArrayList<HistorySample>();
// next sample to be triggered
int head = 0;

boolean run = false;

int x1 = 100;
int y1 = 100;
int x2 = 540;
int y2 = 380;
int corn = 0;

void setup() {
  size( 640, 480 );
  frameRate( 20 );

  // init video related settings	
  cam = new Capture( this, width, height, 20 );
  numPixels = cam.width * cam.height;
  previousFrame = new float[numPixels];
  loadPixels();

  minim = new Minim( this );
  // TODO check the final folders structure
  // load all wav files into the jukebox
  File file = new File("/Users/macbookpro/Desktop/Komornicka_final/src/history/data/");
  String[] files = file.list();
  println( files );
  for ( int i = 0; i < files.length; ++i ) {
    // match regex to be sure it's loading only wav files
    if ( match( files[i], "wav" ) != null ) {
      jukebox.add( new HistorySample( minim, files[i] ) );
    }
  }
}

void draw() {

  if ( run ) {
    // was there any movement in the frame
    int mov_sum = analyse_cam();

    if (mov_sum > 0) {
      updatePixels();
    }

    // if movement was dynamic enought - pull the trigger
    if ( mov_sum > 50000 && frameCount - last_trigger > 40 ) { 
      if ( head < jukebox.size() ) {
        jukebox.get(head).trigger();
        head += 1;

        println( "Trigger!" );
      }    	
      else if ( head == jukebox.size() ) {
        for ( HistorySample hs : jukebox ) {
          hs.next_state();
        }
        head += 1;
      }
      last_trigger = frameCount;
    }
  }
  else {
    cam.read();
    image( cam, 0, 0 );
  }

  for ( HistorySample hs : jukebox ) {
    hs.process();
  }  

  if ( jukebox.get( 0 ).get_state() == 6 ) {
    minim.stop();
    exit();
  }

  pushStyle();
  stroke( #ff6000 );
  noFill();
  rectMode( CORNERS );
  rect( x1, y1, x2, y2 );
  popStyle();
}


int analyse_cam() {

  if ( cam.available() ) {
    cam.read();
    cam.loadPixels();

    int movement_sum = 0;
    for (int i = 0; i < numPixels; ++i) {
      int hor = i % width;
      int ver = i / width;

      if ( hor < x1 || hor > x2 || ver < y1 || ver > y2 ) {
        continue;
      }
      color currColor = cam.pixels[i];

      int currR = (currColor >> 16) & 0xFF; // Like red(), but faster
      int currG = (currColor >> 8) & 0xFF;
      int currB = currColor & 0xFF;

      float currGray = 0.2989 * currR + 0.5870 * currG + 0.1140 * currB;
      float prevGray = previousFrame[i];
      float diffGray = abs(currGray - prevGray);

      int display = diffGray > 20 ? (int)diffGray : 0;
      movement_sum += display;
      pixels[i] = 0xff000000 | display << 16 | display << 8 | display;

      previousFrame[i] = currGray;
    }
    return movement_sum;
  }
  return -1;
}


void mouseReleased() {
  if ( corn == 0 ) {
    x1 = mouseX;
    y1 = mouseY;
    corn = 1;
  }
  else {
    x2 = mouseX;
    y2 = mouseY;
    corn = 0;
  }
}

void keyReleased() {
  if ( key == ' ' ) {
    run = true;
    last_trigger = frameCount;
  }
  if ( key == 't' ) {
    if ( head < jukebox.size() ) {
      jukebox.get(head).trigger();
      head += 1;

      println( "Trigger!" );
    }    	
    else if ( head == jukebox.size() ) {
      for ( HistorySample hs : jukebox ) {
        hs.next_state();
      }
      head += 1;
    }
    last_trigger = frameCount;
  }
}

