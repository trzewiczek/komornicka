s.quit
s.boot
(
b = Buffer.read(s, "/home/beet/code/hexapane/src/engine/anita_solo_long.wav");

SynthDef.new( "player", {|bus=#[0,0], bufnum=0, gain=1|    
    Out.ar( bus, 
        PlayBuf.ar( 1, bufnum, BufRateScale.kr( bufnum ), loop: 1 ) * gain
    );
}).send(s);

SynthDef.new( "chocker", {|bus,lc,rc,a,d,r,del|
    Out.ar( 0, Pan2.ar( 
                  DelayL.ar( In.ar( bus ), 3, del, 0.5 ),//, In.ar( bus ) ), 
                  Line.kr( lc, rc, a+d+r ) 
               )
               * 
               EnvGen.ar( Env.new([0,1,1,0], [a,d,r], 2), doneAction: 2) );
}).send(s);
)

(
~trans = Bus.audio( s, 1 );
k = Synth.new( "player", [\bus, [0,1], \bufnum, b.bufnum, \gain, 0.3] );


x = Synth.new( "player", [\bus, [~trans,2], \bufnum, b.bufnum] );

//~ro = 
Routine({
        var a, d, r;
        var lc, rc;
        var del;

        loop {
            a = 0.3 + 0.7.rand;
            d = 0.1 + 0.9.rand;
            r = 0.3 + 0.7.rand;

            lc = 2.0.rand - 1.0;
            rc = 2.0.rand - 1.0;

            del = 0.5+2.0.rand;

            lc.post; " :: ".post; rc.postln;

            Synth.after( x, "chocker", [ \bus, ~trans, 
                                         \a,   a, 
                                         \d,   d, 
                                         \r,   r,
                                         \lc,  lc,
                                         \rc,  rc,
                                         \del, del ]
            );

            (a+d+r).wait;
        }
    }).play;
//~ro.play;
)

x.free;
~ro.stop




(
b = Buffer.read(s, "/home/beet/code/hexapane/src/engine/anita_solo_long.wav");

{ TGrains.ar( 2, 
              Dust.kr( 50 ),// MouseX.kr( 0.5, 5, 1 ) ),
              b, 
              1, 
              Line.kr( 8, 24, 1 )
              )
  *
  EnvGen.ar( Env.new( [0,1,0], [0.2,0.8], -5 ) )
}.play
)




SynthDef.new( "doubler", {|in_bus|
    var in = In.ar( in_bus, 1 );

});




(
b = Buffer.read(s, "/home/beet/code/hexapane/src/engine/anita_solo_long.wav");

{
p = PlayBuf.ar( 1, b.bufnum, BufRateScale.kr( b.bufnum ), loop: 1 );
Out.ar( 0, [ p,p ] 
           * 
           EnvGen.ar( Env.new( [ 1, 1, 0.4, 0.4, 1, 1, 0.4, 0.4, 1, 1 ], 
                               [ 0.7, 0.35, 1.5, 0.05, 7.9, 0.35, 2, 0.05, 2 ] 
                               ), doneAction: 2 ));

Out.ar( 0, [ DelayL.ar( PitchShift.ar(p, 0.1, 1.005, 0, 0.004), 0.5, 0.005  ),
             PitchShift.ar(p, 0.1, 0.99, 0, 0.004) ]
           * 
           EnvGen.ar( Env.new( [ 0, 0, 1, 1, 0, 0, 1, 1, 0, 0 ], 
                               [ 1,0.05,1.5,0.05,8.2,0.03,2,0.05,2 ] ), doneAction: 2 ));

}.play
)


(
b = Buffer.read(s, "/home/beet/code/hexapane/src/engine/anita_solo_long.wav");

{
p = PlayBuf.ar( 1, b.bufnum, BufRateScale.kr( b.bufnum ), loop: 1 );
Out.ar( 0, [ p,p ] );

}.play
)





(
b = Buffer.read(s, "/home/beet/code/hexapane/src/engine/anita_solo_long.wav");
c = Bus.audio( s, 1 );

SynthDef( "player", {|bus|
    Out.ar( bus, 
            PlayBuf.ar( 1, b.bufnum, BufRateScale.kr( b.bufnum ), loop: 1 )
    );
}).send(s);


SynthDef("help-SendTrig",{|bus|
    var in, amp, d_amp, trig, timer;

    in    = In.ar( bus );
    amp   = Amplitude.kr( in );
    d_amp = Amplitude.kr( DelayL.kr( in, 1, 0.1 ) );
    trig  = ( d_amp - amp ) > 0.2;
    timer = Timer.kr( trig );

    SendTrig.kr( trig, 0, amp );
}).send(s);

// register to receive this message
OSCresponder(s.addr,'/tr',{ arg time,responder,msg;
    [time,responder,msg].postln;
}).add;
)

(
~player1 = Synth.new( "player", [\bus, 0] );
~player2 = Synth.new( "player", [\bus, c] );
~trig = Synth.after( ~player2, "help-SendTrig", [\bus, c] );
)
