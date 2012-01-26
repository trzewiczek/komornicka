#!/usr/bin/python2
from Tkinter import *
import tkFont
import math
import liblo

target = liblo.Address( 5600 )
defaults = {
    'bd': 0,
    'bg': '#2b2b2b',
    'highlightthickness': 0
}

class Track:
    def __init__( self, master, inx ):
        frame = Frame( master, **defaults )
        frame['padx'] = 15
        frame.pack( side=LEFT ) #.grid( row=1 )

        self.base_url = '/track/%d/' % inx

        # mute button for the whole track
        opts = {
            'width': 100,
            'height': 20,
            'text': 'TRACK %d' % inx,
            'on': False,
            'url': '/track/%d/on/' % inx
        }
        onoff = Toggle( frame, **opts )
        onoff.pack()

        # mute button to hexapanel separator
        opts = { 'height': 10 }
        opts.update( defaults )
        Frame( frame, **opts ).pack()

        # hexapanel
        self.cv = Canvas( frame, bd=0, highlightthickness=0 )
        self.cv['bg']     = '#2b2b2b'
        self.cv['width']  = 130
        self.cv['height'] = 100
        self.cv.pack()

        # hexapanel background image and description
        self.cv.create_rectangle( 15, 0, 115, 100, fill='#3b3b3b', outline='' )
        self.cv.create_line( 15, 0, 115, 100, fill='#4b4b4b', smooth=True )
        self.cv.create_line( 15, 100, 115, 0, fill='#4b4b4b', smooth=True )
        self.cv.create_line( 65, 0, 65, 100, fill='#4b4b4b', smooth=True )
        self.cv.create_line( 15, 50, 115, 50, fill='#4b4b4b', smooth=True )

        self.font = tkFont.Font(family='DejaVu Mono', size=7)
        self.cv.create_rectangle( 45, 4, 85, 15, fill='#3b3b3b', outline='' )
        self.cv.create_text( 65, 10, text='SCENA', font=self.font, fill='#5b5b5b', anchor='c' )

        # define speakers
        self.speakers = [
            {
                'x':   0, 'y':   0, 'a': 'nw',
                't': Toggle( frame, on=True, url=self.base_url+'speaker/0/on/' )
            },
            {
                'x': 100, 'y':   0, 'a': 'ne',
                't': Toggle( frame, on=True, url=self.base_url+'speaker/1/on/' )
            },
            {
                'x':   0, 'y':  50, 'a':  'w',
                't': Toggle( frame, on=True, url=self.base_url+'speaker/2/on/' )
            },
            {
                'x': 100, 'y':  50, 'a':  'e',
                't': Toggle( frame, on=True, url=self.base_url+'speaker/3/on/' )
            },
            {
                'x':   0, 'y': 100, 'a': 'sw',
                't': Toggle( frame, on=True, url=self.base_url+'speaker/4/on/' )
            },
            {
                'x': 100, 'y': 100, 'a': 'se',
                't': Toggle( frame, on=True, url=self.base_url+'speaker/5/on/' )
            }
        ]

        # create gui for the speakers
        for s in self.speakers:
            self.cv.create_window( s['x'] + 30 if s['x'] == 100 else s['x'], s['y'],
                                   width=10, height=10, anchor=s['a'],
                                   window=s['t'] )

        # create panorama head
        self.head = self.cv.create_rectangle( 62, 47, 68, 53,
                                              fill='#6bbb7b', outline='' )
        # listen to the head's move
        self.cv.bind('<B1-Motion>', self.move_head )

        # separator
        opts = { 'height': 10 }
        opts.update( defaults )
        Frame( frame, **opts ).pack()

        # inputs
        inputs = Frame( frame, **defaults )
        inputs.pack( side=BOTTOM )
        for i in range( 4 ):
            Toggle( inputs, text=i,
                    width=17, 
                    url='%sinput/%d/on/' % ( self.base_url, i ) ).pack( side=LEFT, padx=5 )


    def move_head( self, event ):
        '''Callback for the head move'''
        # keep head inside the panel
        self.hx = self.constrain( event.x, 18, 112 )
        self.hy = self.constrain( event.y, 3, 97 )

        # move it to a new position
        self.cv.coords( self.head, self.hx-3, self.hy-3, self.hx+3, self.hy+3 )

        # send OSC information about new distance from each speaker
        for i, s in enumerate( self.speakers ):
            url = "%s%s%s" % ( self.base_url, 'head/distance/', i )
            liblo.send( target, url, self.get_distance( s['x'], s['y'] ))


    def constrain( self, n, l, h ):
        if n < l:
            return l
        elif n > h:
            return h
        else:
            return n


    def get_distance( self, sx, sy ):
        '''Counts the distance of the dot from the speakers'''
        x = abs( self.hx - sx - 15 )
        y = abs( self.hy - sy )
        ang = abs( math.atan2( y, x ))

        c = float( x ) / math.cos( ang );

        if sy == 50:
            if ang < 0.46364757:
                C = 100.0 / math.cos( ang )
            else:
                C =  50.0 / math.sin( ang)
        else:
            if ang < 0.7853982:
                C = 100.0 / math.cos( ang )
            else:
                C = 100.0 / math.sin( ang)

        return c / C



class Toggle( Frame ):
    def __init__( self, master, **options ):
        self.master     = master
        self.options    = options

        self._extract_options( options )
        bg = '#7b7b7b' if self.on else '#4e4e4e'

        self.width      = options.setdefault('width', 100)
        self.height     = options.setdefault('height', 15)
        self.background = options.setdefault('background', bg)
        self.relief     = options.setdefault('relief', 'flat')
        self.bd         = options.setdefault('bd', 0)
        self.hlt        = options.setdefault('highlightthickness', 0)


        Frame.__init__( self, master, options )
        opts = {
            'width': self.width,
            'height': self.height,
            'bd': self.bd,
            'bg': self.background,
            'highlightthickness': self.hlt
        }
        self.cv = Canvas( self, **opts )

        opts = {
            'font': tkFont.Font( family='DejaVu Sans', size=6 ),
            'fill': '#ddd' if self.on else '#9b9b9b',
            'text': self.text,
            'anchor': 'c'
        }

        self.label = self.cv.create_text( self.width/2, self.height/2, **opts )
        self.cv.pack()

        self.cv.bind( '<Button-1>', self.toggle )


    def pop( self, dict, key, default ):
        value = dict.get( key, default )

        if dict.has_key( key ):
            del dict[ key ]

        return value


    def _extract_options(self, options):
        # these are the options not applicable to a frame
        self.text = self.pop( options, 'text', 'ON/OFF')
        self.on   = self.pop( options, 'on', False )
        self.url  = self.pop( options, 'url', '/toggle/on/' )


    def toggle( self, event ):
        self.on = not self.on

        if self.on:
            self.cv['background'] = '#7b7b7b'
            self.cv.itemconfigure( self.label, fill='#fff' )
        else:
            self.cv['background'] = '#4e4e4e'
            self.cv.itemconfigure( self.label, fill='#9b9b9b' )

        liblo.send( target, self.url, 1 if self.on else 0 )

        return self.on




root = Tk()
root['bg'] = '#2b2b2b'
root['padx'] = 25
root['pady'] = 25

for i in range( 6 ):
    Track( root, i )

root.mainloop()
