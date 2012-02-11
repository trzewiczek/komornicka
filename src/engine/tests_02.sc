(
SynthDef.new( \player, {|bus=0, bufnum=0, gain=1|    
    Out.ar( bus, 
        PlayBuf.ar( 1, bufnum, BufRateScale.kr( bufnum ), loop: 1 ) * gain
    );
}).send(s);

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

SynthDef.new( \chocker02, {|bus, len=1, pan=0, del=0, gate=0|
    var a, s, r, in;
    a = 0.3 * len;
    d = 0.5 * len;
    r = 0.2 * len;
    
    in = DelayL.ar( In.ar( bus, 1 ), 5, del );

    Out.ar( 0, Pan2.ar( in
                        * 
                        EnvGen.ar( Env.adsr(a,d,1,r,1,3), gate ), pan ));
}).send(s);

b = Buffer.read(s, "/home/beet/code/hexapane/src/engine/anita_solo_long.wav");

~send = Bus.audio( s, 1 );

~sources = Group.new;
~effects = Group.after( ~sources );
)
(
Synth.new( \player, [\bus, ~send, \bufnum, b.bufnum], ~sources );
Synth.new( \player, [\bus, 0, \bufnum, b.bufnum, \gate, 0.3], ~sources );
Synth.new( \player, [\bus, 1, \bufnum, b.bufnum, \gate, 0.3], ~sources );
x = Synth.new( \chocker02, [\del, 0, \bus, ~send, \len, 0.5, \pan, 0], ~effects );
y = Synth.new( \chocker02, [\del, 0.3, \bus, ~send, \len, 0.5, \pan, 0], ~effects );
z = Synth.new( \chocker02, [\del, 0.7, \bus, ~send, \len, 0.5, \pan, 0], ~effects );
v = Synth.new( \chocker02, [\del, 0.9, \bus, ~send, \len, 0.5, \pan, 0], ~effects );
)
(
Routine({
    var ins = [ x, y, z, v ];
    var synth, len = 0.2 + 0.3.rand;

    loop {
        ins.do({|e|
            e.set( \gate, 0 );
        });
        ( 0.1 * len ).wait;
        5.wait;
        
        len = 0.5 + 0.5.rand;
        synth = ins.choose;

        synth.set( \gate, 1 );
        synth.set( \len, len );

        len.wait;
    };
}).play;
)



( // CHOCKER 02
Routine({
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
}).play;
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



