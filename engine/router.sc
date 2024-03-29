(
SynthDef.new( "player", {|bus=0, bufnum=0, del=0|    
    Out.ar( bus, AudioIn.ar( bufnum ) );
}).send(s);

SynthDef.new( "HexaPane", {
    arg ins  = #[0,0,0,0], 
        buss = #[5,5,5,5],
        dist = #[0.16,0.16,0.16,0.16,0.16,0.16], 
        spks = #[1,1,1,1,1,1], 
        gate = 1, master = 1;

    // inputs have both - buf indicies and gates!!
    var input = buss.collect {|e,i| In.ar( e, 1 ) * ins[i] };
    var final = spks.size.collect {|i| Mix.ar( input ) * spks[i] * dist[i] };
    
    Out.ar( 2, final * master * gate );
}).send(s);

~i1 = Bus.audio( s, 1 );
~i2 = Bus.audio( s, 1 );
~i3 = Bus.audio( s, 1 );
~i4 = Bus.audio( s, 1 );

~sources = Group.new;
~effects = Group.after( ~sources );

~osc_adder = {|track,inx|
    track.post;
    " :: ".post;
    inx.postln;
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
)


(
y = Synth.new( "player", [\bus, ~i1, \bufnum, 0], ~sources );
z = Synth.new( "player", [\bus, ~i2, \bufnum, 1], ~sources );
v = Synth.new( "player", [\bus, ~i3, \bufnum, 2], ~sources );
w = Synth.new( "player", [\bus, ~i4, \bufnum, 3], ~sources );

~t6 = Synth.new( "HexaPane", [\buss, [~i1,~i2,~i3,~i4]], ~effects );
~t5 = Synth.new( "HexaPane", [\buss, [~i1,~i2,~i3,~i4]], ~effects );
~t4 = Synth.new( "HexaPane", [\buss, [~i1,~i2,~i3,~i4]], ~effects );
~t3 = Synth.new( "HexaPane", [\buss, [~i1,~i2,~i3,~i4]], ~effects );
~t2 = Synth.new( "HexaPane", [\buss, [~i1,~i2,~i3,~i4]], ~effects );
~t1 = Synth.new( "HexaPane", [\buss, [~i1,~i2,~i3,~i4]], ~effects );

~osc_adder.value( ~t1, 0 );
~osc_adder.value( ~t2, 1 );
~osc_adder.value( ~t3, 2 );
~osc_adder.value( ~t4, 3 );
~osc_adder.value( ~t5, 4 );
~osc_adder.value( ~t6, 5 );
)





(
//~t6 = Synth.new( "HexaPane", [\buss, [~i1,~i2,~i3,~i4]] );
//~t5 = Synth.before( ~t6, "HexaPane", [\buss, [~i1,~i2,~i3,~i4]] );
//~t4 = Synth.before( ~t5, "HexaPane", [\buss, [~i1,~i2,~i3,~i4]] );
//~t3 = Synth.before( ~t4, "HexaPane", [\buss, [~i1,~i2,~i3,~i4]] );
//~t2 = Synth.before( ~t3, "HexaPane", [\buss, [~i1,~i2,~i3,~i4]] );
//~t1 = Synth.before( ~t2, "HexaPane", [\buss, [~i1,~i2,~i3,~i4]] );
//
//y = Synth.before( ~t1, "player", [\bus, ~i1, \bufnum, b.bufnum] );
//z = Synth.before( ~t1, "player", [\bus, ~i2, \del, 0.5, \bufnum, b.bufnum] );
//v = Synth.before( ~t1, "player", [\bus, ~i3, \del, 1.1, \bufnum, b.bufnum] );
//w = Synth.before( ~t1, "player", [\bus, ~i4, \del, 1.4, \bufnum, b.bufnum] );
)
