s.boot;
(
b = Buffer.read(s, "/home/beet/code/hexapane/src/engine/anita_solo_long.wav");
~voice = Bus.audio( s, 1 );
~pitch = Bus.audio( s, 1 );

~inputs  = Group.new;
~sources = Group.after( ~inputs );
~effects = Group.after( ~sources );

Synth.new( \player, [\out, 0, \bufnum, b.bufnum, \gain, 0.3], ~inputs );
Synth.new( \player, [\out, ~voice, \bufnum, b.bufnum], ~inputs );
Synth.new( \voices, [\out, ~pitch, \in, ~voice], ~sources );
//Synth.new( \pitch_trigger, [\out, 1, \c_in, ~voice, \a_in, ~pitch, \a, 0.5, \r, 0.5 ], ~effects );
Synth.new( \pause_trigger, [\out, 1, \c_in, ~voice, \a_in, ~pitch, \a, 0.5, \r, 0.5 ], ~effects );

)


( // C H O C K E R   R O U T I N E   
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
