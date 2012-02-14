(
SynthDef( \sine, {|freq=100, bus=0|
    Out.ar( bus, SinOsc.ar( freq, mul: 0.2 ) );
}).send( s );

SynthDef( \reciver, {|bus, out=0, pan=0, freq=8|
    var in = In.ar( bus, 1 );

    Out.ar( 0, Pan2.ar( in * SinOsc.ar( freq, mul: 0.5, add: 0.5 ), pan ) );
}).send( s );

~bus = Bus.audio( s, 1 );

~sources = Group.new();
~effects = Group.after( ~sources );
)


( 
Synth.new( \sine, [\bus, ~bus], ~sources );
Synth.new( \reciver, [\bus, ~bus, \pan, 1], ~effects );
Synth.new( \reciver, [\bus, ~bus, \pan, -1, \freq, 12], ~effects );
Synth.new( \reciver, [\bus, ~bus, \pan, -0.4, \freq, 11], ~effects );
Synth.new( \reciver, [\bus, ~bus, \pan, 0.4, \freq, 15], ~effects );
)

