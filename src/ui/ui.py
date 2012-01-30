#!/usr/bin/python2
from Tkinter import *
import tkFont
import math
import sys
import pickle
import OSC

# OSC setup
send_address = '127.0.0.1', 5600
c = OSC.OSCClient()
c.connect( send_address )

try:
    DEBUG = True if sys.argv[1] == 'DEBUG' else False
except:
    DEBUG = False

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
            'url': '/track/%d/on' % inx
        }
        self.onoff = Toggle( frame, **opts )
        self.onoff.pack()

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
                't': Toggle( frame, on=True, url=self.base_url+'speaker/0/on' )
            },
            {
                'x': 100, 'y':   0, 'a': 'ne',
                't': Toggle( frame, on=True, url=self.base_url+'speaker/1/on' )
            },
            {
                'x':   0, 'y':  50, 'a':  'w',
                't': Toggle( frame, on=True, url=self.base_url+'speaker/2/on' )
            },
            {
                'x': 100, 'y':  50, 'a':  'e',
                't': Toggle( frame, on=True, url=self.base_url+'speaker/3/on' )
            },
            {
                'x':   0, 'y': 100, 'a': 'sw',
                't': Toggle( frame, on=True, url=self.base_url+'speaker/4/on' )
            },
            {
                'x': 100, 'y': 100, 'a': 'se',
                't': Toggle( frame, on=True, url=self.base_url+'speaker/5/on' )
            }
        ]

        # create gui for the speakers
        for s in self.speakers:
            self.cv.create_window( s['x'] + 30 if s['x'] == 100 else s['x'], s['y'],
                                   width=10, height=10, anchor=s['a'],
                                   window=s['t'] )

        # create panorama head
        self.hx = 65
        self.hy = 50
        self.head = self.cv.create_rectangle( self.hx - 3, self.hy - 3,
                                              self.hx + 3, self.hy + 3,
                                              fill='#6bbb7b', outline='' )
        # listen to the head's move
        self.cv.bind('<B1-Motion>', self.move_head )

        # separator
        opts = { 'height': 10 }
        opts.update( defaults )
        Frame( frame, **opts ).pack()

        # inputs container
        inputs_frame = Frame( frame, **defaults )
        inputs_frame.pack()# side=BOTTOM )
        # create and collect references to inputs objects
        self.inputs = []
        for i in range( 4 ):
            self.inputs.append( Toggle( inputs_frame, text=i,
                                width=17,
                                url='%sinput/%d/on' % ( self.base_url, i ) ) )
            self.inputs[-1].pack( side=LEFT, padx=5 )

        self.master_cv = Canvas( frame, **defaults )
        self.master_cv['bg']     = '#3b3b3b'
        self.master_cv['width']  = 100
        self.master_cv['height'] = 10
        self.master_cv.pack( pady=10 )

        self.mx = 5
        self.master = self.master_cv.create_rectangle( self.mx-5, 0, self.mx+5, 10,
                                                       fill='#9b9b9b', outline='' )

        self.master_cv.bind('<B1-Motion>', self.move_master )


    def set_state( self, state={} ):
        try:
            self.base_url = state['base_url']
        except:
            pass

        self.mx = state.get('master', 5)
        self.master_cv.coords( self.master, self.mx-5, 0, self.mx+5, 10 )

        # move it to a new position
        self.hx, self.hy = state.get('head_position', (65, 50))
        self.cv.coords( self.head, self.hx-3, self.hy-3, self.hx+3, self.hy+3 )

        # mute buttom
        self.onoff.set_state( state.get('mute', False) )

        # TODO see how to map two lists at once
        speakers_state = state.get( 'speakers', [1 for s in self.speakers] )
        for speaker, stored_state in zip( self.speakers, speakers_state ):
            speaker['t'].set_state( stored_state )

        # TODO see how to map two lists at once
        inputs_state = state.get( 'inputs', [0 for i in self.inputs] )
        for input, stored_state in zip( self.inputs, inputs_state ):
            input.set_state( stored_state )


        self.send_state()


    def get_state( self ):
        return {
            'base_url': self.base_url,
            'master': self.mx,
            'head_position': ( self.hx, self.hy ),
            'speakers': map( (lambda e: e['t'].get_state()), self.speakers ),
            'inputs': map( Toggle.get_state, self.inputs ),
            'mute': self.onoff.get_state()
        }


    def send_state( self ):
        # track mute button
        self.onoff.send_state()

        # speakers on/off
        for s in self.speakers:
            s['t'].send_state()

        # inputs
        map( Toggle.send_state, self.inputs )

        # send OSC information about new distance from each speaker
        # prepare message
        msg = OSC.OSCMessage()
        msg.setAddress( self.base_url + 'head/distance' )
        for i, s in enumerate( self.speakers ):
            msg.append( 1.0 - self.get_distance( s['x'], s['y'] ) )

            if DEBUG:
                print '%s :: %s' % ( url, self.get_distance( s['x'], s['y'] ))

        # send message
        c.send( msg )

        self.move_master()


    def move_master( self, event=None ):
        '''Callback for the head move'''
        try:
            # keep master inside range
            self.mx = self.constrain( event.x, 5, 95 )
        except:
            pass

        # move it to a new position
        self.master_cv.coords( self.master, self.mx-5, 0, self.mx+5, 10 )

        # send OSC information about new distance from each speaker
        # prepare message
        msg = OSC.OSCMessage()
        msg.setAddress( self.base_url + 'master' )
        # normalize the master range
        normalized_value = ( self.mx - 5 ) / 90.0
        msg.append( normalized_value )

        # send message
        c.send( msg )


    def move_head( self, event ):
        '''Callback for the head move'''
        # keep head inside the panel
        self.hx = self.constrain( event.x, 18, 112 )
        self.hy = self.constrain( event.y, 3, 97 )

        # move it to a new position
        self.cv.coords( self.head, self.hx-3, self.hy-3, self.hx+3, self.hy+3 )

        # send OSC information about new distance from each speaker
        # prepare message
        msg = OSC.OSCMessage()
        msg.setAddress( self.base_url + 'head/distance' )
        for i, s in enumerate( self.speakers ):
            msg.append( 1.0 - self.get_distance( s['x'], s['y'] ) )

        # send message
        c.send( msg )


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
        self.url  = self.pop( options, 'url', '/toggle/on' )


    def set_state( self, state ):
        self.on = not bool( state )
        self.toggle()

    def get_state( self ):
        return self.on

    def send_state( self ):
        if DEBUG:
            print "%s :: %s" % ( self.url, 1 if self.on else 0 )
        msg = OSC.OSCMessage()
        msg.setAddress( self.url )
        msg.append( 1 if self.on else 0 )

        c.send( msg )


    def toggle( self, event=None ):
        self.on = not self.on

        if self.on:
            self.cv['background'] = '#7b7b7b'
            self.cv.itemconfigure( self.label, fill='#fff' )
        else:
            self.cv['background'] = '#4e4e4e'
            self.cv.itemconfigure( self.label, fill='#9b9b9b' )

        if DEBUG:
            print "%s :: %s" % ( self.url, 1 if self.on else 0 )

        msg = OSC.OSCMessage()
        msg.setAddress( self.url )
        msg.append( 1 if self.on else 0 )

        c.send( msg )

        return self.on

class StateManager:
    def __init__( self, master, tracks ):
        frame = Frame( master, **defaults )
        frame['pady'] = 15
        frame.pack( side=LEFT )

        self.tracks = tracks

        try:
            self.scenes = pickle.load( open('scenes.db', 'rb') )
            # TODO make it run on first list from scenes!
            for t, s in zip( tracks, self.scenes ):
                t.set_state( s )
        except:
            # initialize scenes with a current init state
            self.scenes = [ [ e.get_state() for e in tracks ] ]

        if DEBUG:
            for t in tracks:
                t.send_state()

        self.current_scene = 0
        self.scenes_switch = []
        for i in range( len( self.scenes) ):
            opts = {
                'text': i,
                'width': 15
            }
            opts.update( defaults )
            t = Toggle( frame, **opts )
            t.pack( side=LEFT )

            self.scenes_switch.append( t )

        # highlight the current (first?) scene
        self.scenes_switch[ self.current_scene ].toggle()
        frame.bind('<Button-1>', self.click )

        opts = { 'width':15 }
        opts.update( defaults )
        Frame( master, **opts ).pack( side=LEFT )

        save = Button( state_frame, **defaults )
        save['fg'] = '#9b9b9b'
        save['text'] = 'SAVE'
        save['command'] = self.save_state
        save.pack( side=LEFT )

        reset = Button( state_frame, **defaults )
        reset['fg'] = '#9b9b9b'
        reset['text'] = 'RESET'
        reset['command'] = self.reset_state
        reset.pack( side=LEFT )


    def click( self, event ):
        map( Toggle.set_state, self.scenes_switch )


    def reset_state( self ):
        map( Track.set_state, tracks )


    def save_state( self ):
        state = [ e.get_state() for e in self.tracks ]
        try:
            pickle.dump( state, open('scenes.db', 'wb') )

            if DEBUG:
                print "State save!"

        except Exception as e:
            print "-----------------"
            print ">> Can't save the state"
            print e
            print "-----------------"



root = Tk()
root['bg'] = '#2b2b2b'
root['padx'] = 25
root['pady'] = 25


# tracks container
tracks_frame = Frame( root, **defaults )
tracks_frame.pack()

# TODO why's that?
# a global keeping all tracks references
tracks = []
for i in range( 6 ):
    tracks.append( Track( tracks_frame, i ) )

# scenes switcher container
state_frame = Frame( root, **defaults )
state_frame.pack( side=LEFT, pady=15 )

StateManager( state_frame, tracks )

root.mainloop()
