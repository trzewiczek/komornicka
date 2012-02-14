
// 

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
