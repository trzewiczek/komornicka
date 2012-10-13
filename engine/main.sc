(
var win;
var trig_freq;
var francaise;

// in case too less memory for SuperCollider
//Server.default.options.memSize = 2 ** 19;

// buses
~mic_in = Bus.audio( s, 1 );
~pitch  = Bus.audio( s, 1 );
~pause  = Bus.audio( s, 1 );
~gate  = Bus.control( s, 1 );

~inputs  = Group.new;
~sources = Group.after( ~inputs );
~effects = Group.after( ~sources );

// buffer for the returning voices
~buffer1 = Buffer.alloc( s, s.sampleRate * 60, 1 );
~buffer2 = Buffer.alloc( s, s.sampleRate * 60, 1 );
~buffer3 = Buffer.alloc( s, s.sampleRate * 60, 1 );
~buffer4 = Buffer.alloc( s, s.sampleRate * 60, 1 );
~buffer5 = Buffer.alloc( s, s.sampleRate * 60, 1 );
~buffer6 = Buffer.alloc( s, s.sampleRate * 60, 1 );

// microphone input
~mic_fx = Synth.new( \mic, [\out, ~mic_in, \gain, 0], ~inputs );
~mic_dd = Synth.new( \mic, [\out, 2, \gain, 0], ~inputs );
~mic_dl = Synth.new( \mic, [\out, 4, \gain, 0], ~inputs );
~mic_dr = Synth.new( \mic, [\out, 5, \gain, 0], ~inputs );

~git_01 = Synth.new( \line_in, [\out, 2, \a_in, 3, \gain, 0], ~inputs );
~git_02 = Synth.new( \line_in, [\out, 3, \a_in, 3, \gain, 0], ~inputs );
~git_03 = Synth.new( \line_in, [\out, 6, \a_in, 3, \gain, 0], ~inputs );
~git_04 = Synth.new( \line_in, [\out, 7, \a_in, 3, \gain, 0], ~inputs );

// pitch trigger
~pit = Synth.new( \pitch_trigger, [\out, ~pitch, \c_in, ~mic_in, \a_in, ~mic_in, \a, 0.3, \r, 1 ], ~sources );
Synth.new( \wider, [\out, 2, \a_in, ~pitch], ~effects );


// pause trigger
~pat = Synth.new( \pause_trigger, [\out, ~pause, \c_out, ~gate, \c_in, ~mic_in, \a_in, ~mic_in, \a, 0.3, \r, 2 ], ~sources );
~v1 = Synth.new( \voicer, [\out, 3, \a_in, ~mic_in, \c_in, ~gate, \del, rrand(15,25), \env_del, rrand(0.0,2.0), \buf, ~buffer2], ~effects );
~v2 = Synth.new( \voicer, [\out, 5, \a_in, ~mic_in, \c_in, ~gate, \del, rrand(35,45), \env_del, rrand(0.0,2.0), \buf, ~buffer4], ~effects );
~v3 = Synth.new( \voicer, [\out, 6, \a_in, ~mic_in, \c_in, ~gate, \del, rrand(45,55), \env_del, rrand(0.0,2.0), \buf, ~buffer5], ~effects );


~mad_gain = 0.0;
Routine({
	var start = rrand( -1.0, 0 );
	var end = rrand( 0, 1.0 );
	var dur = rrand( 1, 3 );
	var pos = rrand( -0.9, 0.9 );
	
	loop {
		pos = rrand( -0.9, 0.9 );
		Synth.new( \logic_of_madness, [\start, start, \end, end, \dur, dur, \gain, ~mad_gain, \pos, pos] );
		
		( 0.5 + dur ).wait;
	};
}).play;


// chockers
//~ch1 = Synth.new( \chocker, [\del, 0,   \len, 1, \out, 2, \a_in, ~pause], ~effects );
//~ch2 = Synth.new( \chocker, [\del, 0.3, \len, 1, \out, 3, \a_in, ~pause], ~effects );
//~ch3 = Synth.new( \chocker, [\del, 0.7, \len, 1, \out, 2, \a_in, ~pause], ~effects );
//~ch4 = Synth.new( \chocker, [\del, 0.9, \len, 1, \out, 3, \a_in, ~pause], ~effects );
//
//Routine({
//    var ins = [ ~ch1, ~ch2, ~ch3, ~ch4 ];
//    var synth, len = 0.2 + 0.3.rand;
//
//    synth = ins[0];
//    synth.set( \gate, 1 );
//    synth.set( \len, len );
//
//    0.5 * len.wait;
//        
//    loop {
//        ins.do({|e|
//            e.set( \gate, 0 );
//        });
//        ( 0.1 * len ).wait;
//                
//        len = 0.5 + 0.2.rand;
//        synth = ins.choose;
//
//        synth.set( \gate, 1 );
//        synth.set( \len, len );
//
//        0.5 * len.wait;
//    };
//}).play;



// ROUTER a.k.a Hexapane
~i1 = Bus.audio( s, 1 );
~i2 = Bus.audio( s, 1 );
~i3 = Bus.audio( s, 1 );
~i4 = Bus.audio( s, 1 );

~r_sources = Group.new;
~r_effects = Group.after( ~r_sources );

~osc_adder = {|track,inx|
    track.post; " :: ".post; inx.postln;
    OSCresponderNode( nil, '/track/'++inx++'/on', {|t,r,m|
        track.set( \gate, m[1] );
        m[1].postln;
    }).add; 
    OSCresponderNode( nil, '/track/'++inx++'/master', {|t,r,m|
        track.set( \master, m[1] );
        m[1].postln;
    }).add; 
    OSCresponderNode( nil, '/track/'++inx++'/inputs', {|t,r,m|
        var ins = m.copyRange(1,m.size-1);
        track.setn( \ins, ins );
        ins.postln;
    }).add;
    OSCresponderNode( nil, '/track/'++inx++'/speakers', {|t,r,m|
        var spks = m.copyRange(1,m.size-1);
        track.setn( \spks, spks );
        spks.postln;
    }).add;
    OSCresponderNode( nil, '/track/'++inx++'/head', {|t,r,m|
        var muls = m.copyRange(1,m.size-1).normalizeSum;
        track.setn( \dist, muls );
        muls.postln;
    }).add;
};

~r1 = Synth.new( \line_in, [\out, ~i1, \a_in, 0], ~r_sources );
~r2 = Synth.new( \line_in, [\out, ~i2, \a_in, 1], ~r_sources );
~r3 = Synth.new( \line_in, [\out, ~i3, \a_in, 2], ~r_sources );
~r4 = Synth.new( \line_in, [\out, ~i4, \a_in, 3], ~r_sources );

~t6 = Synth.new( \HexaPane, [\buss, [~i1,~i2,~i3,~i4]], ~r_effects );
~t5 = Synth.new( \HexaPane, [\buss, [~i1,~i2,~i3,~i4]], ~r_effects );
~t4 = Synth.new( \HexaPane, [\buss, [~i1,~i2,~i3,~i4]], ~r_effects );
~t3 = Synth.new( \HexaPane, [\buss, [~i1,~i2,~i3,~i4]], ~r_effects );
~t2 = Synth.new( \HexaPane, [\buss, [~i1,~i2,~i3,~i4]], ~r_effects );
~t1 = Synth.new( \HexaPane, [\buss, [~i1,~i2,~i3,~i4]], ~r_effects );

~osc_adder.value( ~t1, 0 );
~osc_adder.value( ~t2, 1 );
~osc_adder.value( ~t3, 2 );
~osc_adder.value( ~t4, 3 );
~osc_adder.value( ~t5, 4 );
~osc_adder.value( ~t6, 5 );

~madness = Synth.new( \logic_of_madness, [\gain, 0] );

// GUI for the effects
win = Window( "Komornicka", Rect( 3, 390, 360, 130 ) );
win.view.background = Color( 0.8, 0.8, 0.8 );

francaise = Button( win, Rect( 10, 10, 130, 50 ) )
            .states_([
                [ "CARISSIMA", Color.black, Color.white ],
                [ "MAMMA MIA",  Color.white, Color.red ]
            ])
            .action_({|view|
                if( view.value == 1, {
                    ~mic_fx.set( \gain, 0.9 );
//                    ~mic_dd.set( \gain, 0.15 );                    
                    ~mic_dl.set( \gain, 0.25 );
                    ~mic_dr.set( \gain, 0.25 );
                }, {
                    ~mic_fx.set( \gain, 0 );
//                    ~mic_dd.set( \gain, 0 );                    
                    ~mic_dl.set( \gain, 0 );
                    ~mic_dr.set( \gain, 0 );
                });
                view.value.postln;
            });
            
~voicers_gain = Slider( win, Rect( 150, 10, 200, 50 ) )
				  .value_( 1.0 )
				  .action_({
					  var val = ~voicers_gain.value;
					  ~v1.set( \gain, val );
					  ~v2.set( \gain, val );
					  ~v3.set( \gain, val );
					  val.postln;
				  });

~mad_btn = Button( win, Rect( 10, 70, 130, 50 ) )
            .states_([
                [ "LOGIC OF", Color.black, Color.white ],
                [ "MADNESS",  Color.white, Color.red ]
            ])
            .action_({|view|
                if( view.value == 1, {
                    ~mad_gain = 1.0;
                    ~git_01.set( \gain, 0.005 );
                    ~git_02.set( \gain, 0.005 );
                    ~git_03.set( \gain, 0.005 );                    
                    ~git_04.set( \gain, 0.005 );
                }, {
                    ~mad_gain = 0.0;
                    ~git_01.set( \gain, 0 );
                    ~git_02.set( \gain, 0 );
                    ~git_03.set( \gain, 0 );                    
                    ~git_04.set( \gain, 0 );
                });
                view.value.postln;
            });

win.front;
)



{ PinkNoise.ar([0, 0, 1, 1, 1, 1, 1, 1]) }.play
