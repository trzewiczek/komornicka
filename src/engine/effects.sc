(
// P A U S E   T R I G G E R 
SynthDef.new( \pause_trigger, {|out, c_in, a_in, a, d, r, lag=1, thres=0.005|
    var fc_in, fa_in;
    var a1, a2, a3, a4;
    var trig;

    fa_in = In.ar( a_in, 1 );
    fc_in = In.ar( c_in, 1 );

    a1 = Amplitude.kr( fc_in );
    a2 = Amplitude.kr( DelayL.ar( fc_in, 1, lag * 0.3 ) );
    a3 = Amplitude.kr( DelayL.ar( fc_in, 1, lag * 0.6 ) );
    a4 = Amplitude.kr( DelayL.ar( fc_in, 1, lag ) );

    trig = ( a1 < thres ) * ( a2 < thres ) * ( a3 < thres ) * ( a4 < thres );

    Out.ar( out, fa_in * EnvGen.ar( Env.adsr( a,d,1,r, 1, 3 ), trig ) );
}).send(s);


// P I T C H   T R I G G E R 
SynthDef.new( \pitch_trigger, {|out, c_in, a_in, a, d, r, lag=1, thres=0.005|
    var fc_in, fa_in;
    var freq, hasFreq;
    var trig;

    fa_in = In.ar( a_in, 1 );
    fc_in = In.ar( c_in, 1 );
    # freq, hasFreq = Pitch.kr( fc_in );

    trig = freq < 1000 * freq > 370;

    Out.ar( out, fa_in * EnvGen.ar( Env.adsr( a,d,1,r, 1, 3 ), trig ) );
}).send(s);


// C H O C K E R
SynthDef.new( \chocker, {|bus, len=1, pan=0, del=0, gate=0, out=0, fx|
    var a, s, r, in;
    a = 0.3 * len;
    d = 0.5 * len;
    r = 0.2 * len;
    
    in = DelayL.ar( In.ar( bus, 1 ), 5, del );
    Out.ar( fx, in * EnvGen.ar( Env.adsr(a,d,1,r,1,3), gate ));
    Out.ar( out, Pan2.ar( in
                        * 
                        EnvGen.ar( Env.adsr(a,d,1,r,1,3), gate ), pan ));
}).send(s);


// V O I C E   M U L T I P L I E R
SynthDef.new( \voices, {|out=0,in|
    var fa_in = In.ar( in, 1 );
    
    Out.ar( out,
            TGrains.ar( 6,                          // num of channels
                        Dust.kr( 3 ),               // trigger
                        fa_in,                      // buffer
                        1,                          // rate
                        LFNoise0.kr( 1, 16, 16 ),   // center position
                        3                           // sample duration
            ));
}).send(s);


// W I D E   P A N O R A M A 
SynthDef.new( \wider, {|out=0,a_in|
    var fa_in;
    var sig1, sig2, sig3, sig4, sig5, sig6; 

    fa_in = In.ar( a_in, 1 );

    sig1 = DelayC.ar( sig1, 0.1, 0.005 );
    sig2 = PitchShift.ar( fa_in, 0.1, 0.99, 0, 0.004 );
    sig3 = fa_in;
    sig4 = fa_in;
    sig5 = DelayC.ar( fa_in, 0.1, 0.01 );
    sig6 = PitchShift.ar( fa_in, 0.1, 1.005, 0, 0.004 );

    Out.ar( out, [ sig1, sig2, sig3, sig4, sig5, sig6 ] );
}).send(s);

SynthDef.new( \player, {|bus=0, bufnum=0, gain=1|    
    Out.ar( bus, 
        PlayBuf.ar( 1, bufnum, BufRateScale.kr( bufnum ), loop: 1 ) * gain
    );
}).send(s);

)
