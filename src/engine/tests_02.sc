s.boot;
(
SynthDef.new( \player, {|bus=0, bufnum=0, gain=1|    
    Out.ar( bus, 
        PlayBuf.ar( 1, bufnum, BufRateScale.kr( bufnum ), loop: 1 ) * gain
    );
}).send(s);

SynthDef.new( \chocker02, {|bus, len=1, pan=0, del=0, gate=0, out=0, fx|
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

SynthDef.new( \voiceTrigger, {|gate=0, bus|
    var in;
    in = In.ar( bus, 1 );

    Out.ar( 0, Pan2.ar( in
                        * 
                        EnvGen.ar( Env.adsr(0.3,0.1,1,0.4,1,3), gate ), 0 ));
}).send(s);
    


b = Buffer.read(s, "/home/beet/Pulpit/anita_solo_long.wav");
//b = Buffer.read(s, "C:\\sample\\anita_solo_01.wav");

~send = Bus.audio( s, 1 );
~fx   = Bus.audio( s, 1 );
~sources = Group.new;
~effects = Group.after( ~sources );
~gates   = Group.after( ~effects );
)
(
Synth.new( \player, [\bus, ~send, \bufnum, b.bufnum], ~sources );
//Synth.new( \player, [\bus, 0, \bufnum, b.bufnum, \gain, 0.3], ~sources );
//Synth.new( \player, [\bus, 1, \bufnum, b.bufnum, \gain, 0.3], ~sources );
x = Synth.new( \chocker02, [\del, 0, \bus, ~send, \len, 0.5, \pan, 0, \out, 5, \fx, ~fx], ~effects );
y = Synth.new( \chocker02, [\del, 0.3, \bus, ~send, \len, 0.5, \pan, 0, \out, 5, \fx, ~fx], ~effects );
z = Synth.new( \chocker02, [\del, 0.7, \bus, ~send, \len, 0.5, \pan, 0, \out, 5, \fx, ~fx], ~effects );
v = Synth.new( \chocker02, [\del, 0.9, \bus, ~send, \len, 0.5, \pan, 0, \out, 5, \fx, ~fx], ~effects );
)


( // FINAL CHOCKER
r = Routine({
    var ins = [ x, y, z, v ];
    var synth, len = 0.2 + 0.3.rand;

    loop {
        ins.do({|e|
            e.set( \gate, 0 );
        });
        ( 0.1 * len ).wait;
                
        len = 0.5 + 0.5.rand;
        synth = ins.choose;

        synth.set( \gate, 1 );
        synth.set( \len, len );

        len.wait;
    };
});
r.play;
)

~janek = Synth.new( \voiceTrigger, [\bus, ~fx], ~gates );
~janek.set( \gate, 0 );

r.stop;


( // CHOCKER 02
r = Routine({
    var ins = [ x, y, z, v ];
    var synth, len = 0.2 + 0.3.rand;

    loop {
        ins.do({|e|
            e.set( \gate, 0 );
        });
        ( 0.1 * len ).wait;
        
        len = 0.5 + 0.5.rand;
        synth = ins.choose;

        synth.set( \gate, 1 );
        synth.set( \len, len );

        len.wait;
    };
});
r.play;
)




( // CHOCKER
Routine({
    var len, pan, del;
    loop {
        len = 0.5 + 2.5.rand;
        pan = 1.0 - 2.0.rand;
        del = 0.1 + 0.9.rand;

        Synth.new( \chocker, [\del, del, \bus, ~send, \len, len, \pan, pan], ~effects );

        (len * 0.3).wait;
    };
}).play;
)



       
SynthDef.new( \chocker, {|bus, len=1, pan=0, del=0|
    var a, s, r, in;
    a = len * 0.2;
    s = len * 0.6;
    r = len * 0.2;
    
    in = DelayL.ar( In.ar( bus, 1 ), 1, del );

    Out.ar( 0, Pan2.ar( in
                        * 
                        EnvGen.ar( Env.new([0,1,1,0], [a,s,r], [-3,0,3]), doneAction: 2 ), pan ));
}).send(s);


b = Buffer.read(s, "/home/beet/Pulpit/anita_solo_long.wav");
(
SynthDef.new( \player, {|bus=0, bufnum=0, gain=1|    
    Out.ar( bus, 
        PlayBuf.ar( 1, bufnum, BufRateScale.kr( bufnum ), loop: 1 ) * gain
    );
}).send(s);

{
    var in, freq, hasFreq, sig, p1, p2, gain;
    var amp1, amp2, amp3, amp4, ha1, ha2, timer, trig;
    //var syn = Synth.new( \player, [ \bufnum, b.bufnum ] );
    in = PlayBuf.ar( 1, b.bufnum, BufRateScale.kr( b.bufnum ), loop: 1 );
    # freq, hasFreq = Pitch.kr( in );
    # amp1, ha1 = Pitch.kr( in );
    # amp2, ha2 = Pitch.kr( DelayL.ar( in, 1, 1 ) );

    amp1 = Amplitude.kr( in );
    amp2 = Amplitude.kr( DelayL.ar( in, 1, 0.2 ) );
    amp3 = Amplitude.kr( DelayL.ar( in, 1, 0.4 ) );
    amp4 = Amplitude.kr( DelayL.ar( in, 1, 0.6 ) );
    //p1 = amp2 < 1000 * amp2 > 350; 
    //p2 = amp1 < 300;
    //p1 = freq < 1000 * freq > 370;

    trig = ( amp1 < 0.005 ) * ( amp2 < 0.005 ) * ( amp3 < 0.005 ) * ( amp4 < 0.005 );

    Out.ar( 0, [ in, in ] * 0.4 );
    Out.ar( 0, Pan2.ar( DelayL.ar( in, 2, 2 )
                        * 
                        EnvGen.ar( Env.adsr(0.5,2.1,0.9,0.4,1,3), trig ), 1 ));
}.play;
)


(
{
	var a, e, c, scale;
	a = SinOsc.ar(200, mul: 0.1);	// quadratic noise
    //a = PlayBuf.ar( 1, b.bufnum, BufRateScale.kr( b.bufnum ), loop: 1 );
	e = Amplitude.ar(a);//Slope.ar(a);		// first derivative produces line segments
//	c = Amplitude.ar(DelayL.ar(a, 1, 0.001));//Slope.ar(e);		// second derivative produces constant segments
	scale = 0.0002;	// needed to scale back to +/- 1.0
	[a, e];
}.plot
)
