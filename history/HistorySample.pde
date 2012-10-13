public class HistorySample {

  public HistorySample( Minim m, String name ) {
    state = 0;
    gain  = 0;

    ap = m.loadFile( name );
  }

  public void trigger() {
    ap.loop();
    next_state();    
  }

  public void close() {
    ap.close();
  }
  
  public int get_state() {
    return state;
  }

  public void process() {
    // sustain phase
    if ( state == 2 ) {
      return;
    }

    if ( direction > 0 && gain < destination || direction < 0 && gain > destination ) {
      slide = true;
      gain += speed * direction;
      ap.setGain( gain );
    }
    else {
      if ( slide ) {
        next_state();
      }
      slide = false;
    }
  }

  public void next_state() {
    state += 1;
    println( state );

    switch( state ) {
    case 1:
      direction = -1;
      speed = 0.3;
      destination = -10;
      break;

    case 3: 
      direction = -1;
      speed = random( 0.05, 0.1 );
      destination = -40;
      break;

    case 4: 
      direction = 1;
      speed = random( 0.1, 0.5 );
      destination = random( -30, -10 );
      break;

    case 5: 
      direction = -1;
      speed = random( 0.1, 0.3 );
      destination = -80;
      break;
    
    case 6:
      ap.close();
      break;
    }
     
  }

  int   state;
  float gain;
  int   direction;
  float speed;
  float destination;
  boolean slide;
  AudioPlayer ap;
}

