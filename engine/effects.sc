(
// sound player for testing purpose
SynthDef.new( \player, {|out=2, bufnum=0, gain=1|    
		Out.ar( out,
				 PlayBuf.ar( 1, bufnum, BufRateScale.kr( bufnum ), loop: 1 ) * gain
       );
}).load(s);

SynthDef.new( \mic, {|out=2, gain=1|
	Out.ar( out, AudioIn.ar( 1 ) * gain );
}).load(s);

SynthDef.new( \line_in, {|out=2, a_in, gain=1|
	Out.ar( out, AudioIn.ar( a_in ) * gain );
}).load(s);

SynthDef.new( \HexaPane, {
    arg ins  = #[0,0,0,0], 
        buss = #[5,5,5,5],
        dist = #[0.16,0.16,0.16,0.16,0.16,0.16], 
        spks = #[1,1,1,1,1,1], 
        gate = 1, master = 1;

    // inputs have both - buf indicies and gates!!
    var input = buss.collect {|e,i| In.ar( e, 1 ) * ins[i] };
    var final = spks.size.collect {|i| Mix.ar( input ) * spks[i] * dist[i] };
    
    Out.ar( 2, final * master * gate );
}).load(s);

// P A U S E   T R I G G E R 
SynthDef.new( \pause_trigger, {|out=2, c_out, c_in, a_in, a, d, r, lag=2, thres=0.00015|
    var fc_in, fa_in;
    var a1, a2, a3, a4, a5, a6;
    var trig;

    fa_in = In.ar( a_in, 1 );
    fc_in = In.ar( c_in, 1 );

    a1 = Amplitude.kr( fc_in );
    a2 = Amplitude.kr( DelayL.ar( fc_in, lag, lag * 0.4 ) );
    a3 = Amplitude.kr( DelayL.ar( fc_in, lag, lag * 0.6 ) );
    a4 = Amplitude.kr( DelayL.ar( fc_in, lag, lag * 0.8) );
    a5 = Amplitude.kr( DelayL.ar( fc_in, lag, lag * 0.9 ) );
    a6 = Amplitude.kr( DelayL.ar( fc_in, lag, lag ) );

    trig = ( a1 > thres ) * ( a2 < thres ) * ( a3 < thres ) * ( a4 < thres )
    										     * ( a5 < thres ) * ( a6 < thres );

	Out.kr( c_out, trig );
    //Out.ar( out, fa_in * EnvGen.ar( Env.asr( a, 1, r, 3 ), trig ) );
}).load(s);


// P I T C H   T R I G G E R 
SynthDef.new( \pitch_trigger, {|out=2, c_in, a_in, a, r, thres=550|
    var fc_in, fa_in;
    var freq, hasFreq;

    var trig;

    fa_in = In.ar( a_in, 1 );
    fc_in = In.ar( c_in, 1 );
    # freq, hasFreq = Pitch.kr( fc_in );
    
    trig = freq < 1000 * freq > thres;

    Out.ar( out, fa_in * EnvGen.ar( Env.asr( a, 1, r, 3 ), trig ) );
}).load(s);


// C H O C K E R
SynthDef.new( \chocker, {|out=2, a_in, len=3, del=0, gate=0|
    var fa_in;
    var a, r;
   
    a = 0.3 * len;
    r = 0.7 * len;
    
    fa_in = DelayL.ar( In.ar( a_in, 1 ), 5, del );

    Out.ar( out, fa_in * EnvGen.ar( Env.asr( a, 1, r, 3 ), gate ));
}).load(s);


// V O I C E   M U L T I P L I E R
SynthDef.new( \voicer, {|out=2,buf,a_in,c_in,del=6,env_del=0,gain=1|
//	var fa_in = BufDelayL.ar( buf, In.ar( a_in, 1 ), del );
	var fa_in = DelayL.ar( In.ar( a_in, 1 ), 60, del ) * gain;
	var gate  = In.kr( c_in, 1 );

	Out.ar( out, DelayL.ar( fa_in * 
			 			       EnvGen.ar( Env.asr( 0.5, 1.0, 4.5, 'sine' ), gate ),
			 			       2,
			 			       env_del
			 	   )
	);
//	Out.ar( out, fa_in * EnvGen.ar( Env.asr( 0.5, 0.3, 4.5, 'sine' ), gate ));
}).load(s);


// W I D E   P A N O R A M A 
SynthDef.new( \wider, {|out=2,a_in,gain=0.2|
    var fa_in;
    var sig1, sig2, sig3, sig4, sig5, sig6; 

    fa_in = In.ar( a_in, 1 );

    sig1 = DelayC.ar( fa_in, 0.1, 0.01 );
    sig2 = PitchShift.ar( fa_in, 0.1, 0.95, 0, 0.004 );
    sig3 = fa_in * 0;
    sig4 = fa_in * 0;
    sig5 = DelayC.ar( fa_in, 0.1, 0.02 );
    sig6 = PitchShift.ar( fa_in, 0.1, 1.03, 0, 0.004 );

    Out.ar( out, [ sig1, sig2, sig3, sig4, sig5, sig6 ] * gain );
}).load(s);


SynthDef.new( \logic_of_madness, {|a_out=2,a_in=3,start,end,dur,gain=0,pos=0|
	var fa_in;
	var width = 2;

	fa_in = AudioIn.ar( a_in, 1 ) * gain;

	Out.ar( a_out, PanAz.ar( 6,
							PitchShift.ar( fa_in, 
											 0.1,
											 SinOsc.kr( 0.2, mul: 0.01, add: 1.0 ),
											 0,
											 0.004 ),
							pos,
							0.15,
							width )
				 *
				 EnvGen.ar( Env.new( [0,0.2,0.2,0], [0.5,dur,0.5], 'sine' ),
				 			  doneAction: 2 )
	);								
}).load(s);

)

