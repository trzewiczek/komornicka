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
ArrayList<AudioPlayer> jukebox = new ArrayList<AudioPlayer>();
// next sample to be triggered
int head = 0;

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
  File file = new File("C:\\Documents and Settings\\beet\\Moje dokumenty\\Processing\\Komornicka_historia\\data");
  String[] files = file.list();
  println( files );
  for ( int i = 0; i < files.length; ++i ) {
    // match regex to be sure it's loading only wav files
    if ( match( files[i], "wav" ) != null ) {
      jukebox.add( minim.loadFile( files[i] ) );
    }
  }
  AudioOutput out = minim.getLineOut();
  out.printControls();
}

void draw() {
  // was there any movement in the frame
  int mov_sum = analyse_cam();

  if (mov_sum > 0) {
    updatePixels();
  }
  // if movement was dynamic enought - pull the trigger
  if ( mov_sum > 3000000 && frameCount - last_trigger > 40 ) { 
    //println( ">> " + frameCount + " :: " + mov_sum + " :: " + frameRate );
    if ( head < jukebox.size() ) {
      println( "Trigger!" );      
      jukebox.get(head).loop();
      head += 1;
    }    	
    else if ( head == jukebox.size() ) {
      println( "Fading out the samples" );
      for ( AudioPlayer ap : jukebox ) {
//        ap.shiftGain( 1.0, 0.0, 3000 );
        ap.pause();
      }
      // to stop from retiriggering fade_out
      head += 1;
    }
    last_trigger = frameCount;
  }
}


void stop() {
  for ( AudioPlayer ap : jukebox ) {
    ap.close();
  }
  minim.stop();
}



int analyse_cam() {

  if ( cam.available() ) {
    cam.read();
    cam.loadPixels();

    int movement_sum = 0;
    for (int i = 0; i < numPixels; ++i) {
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




