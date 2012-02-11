#!/usr/bin/python

# TODO recode it in Qt!
from Tkinter import *
import tkFont
import math
import sys
import pickle
import OSC
import random
import multiprocessing as mp5

# OSC setup
send_address = '127.0.0.1', 57120
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
        self.walker   = mp5.Process()

        # mute button for the whole track
        opts = {
            'width': 100,
            'height': 20,
            'text': 'TRACK %d' % inx,
            'on': False,
            'cback': self.send_gate
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
                't': Toggle( frame, on=True, cback=self.send_speakers )
            },
            {
                'x': 100, 'y':   0, 'a': 'ne',
                't': Toggle( frame, on=True, cback=self.send_speakers )
            },
            {
                'x':   0, 'y':  50, 'a':  'w',
                't': Toggle( frame, on=True, cback=self.send_speakers )
            },
            {
                'x': 100, 'y':  50, 'a':  'e',
                't': Toggle( frame, on=True, cback=self.send_speakers )
            },
            {
                'x':   0, 'y': 100, 'a': 'sw',
                't': Toggle( frame, on=True, cback=self.send_speakers )
            },
            {
                'x': 100, 'y': 100, 'a': 'se',
                't': Toggle( frame, on=True, cback=self.send_speakers )
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
        opts = { 'height': 5 }
        opts.update( defaults )
        Frame( frame, **opts ).pack()

        self.master_cv = Canvas( frame, **defaults )
        self.master_cv['bg']     = '#3b3b3b'
        self.master_cv['width']  = 100
        self.master_cv['height'] = 10
        self.master_cv.pack( pady=10 )

        self.mx = 5
        self.master = self.master_cv.create_rectangle( self.mx-5, 0, self.mx+5, 10,
                                                       fill='#9b9b9b', outline='' )

        self.master_cv.bind('<B1-Motion>', self.move_master )

        # separator
        opts = { 'height': 5 }
        opts.update( defaults )
        Frame( frame, **opts ).pack()

        # inputs container
        inputs_frame = Frame( frame, **defaults )
        inputs_frame.pack()
        # create and collect references to inputs objects
        self.inputs = []
        for i in range( 4 ):
            self.inputs.append( Toggle( inputs_frame, text=i,
                                width=17,
                                cback=self.send_inputs ))
            self.inputs[-1].pack( side=LEFT, padx=5 )


        # separator
        opts = { 'height': 5 }
        opts.update( defaults )
        Frame( frame, **opts ).pack()

        self.walker_button = Toggle( frame, text='W', width=25, cback=self.walk_and_stop )
        self.walker_button.pack()

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


    def send_gate( self ):
        msg = OSC.OSCMessage()
        msg.setAddress( self.base_url + 'on' )
        msg.append( int(self.onoff.get_state()) )

        c.send( msg )


    def send_state( self ):
        # track mute button
        self.onoff.send_state()

        # speakers on/off
        for s in self.speakers:
            s['t'].send_state()

        # input
        map( Toggle.send_state, self.inputs )

        # send OSC information about new distance from each speaker
        # prepare message
        msg = OSC.OSCMessage()
        msg.setAddress( self.base_url + 'head' )
        for i, s in enumerate( self.speakers ):
            msg.append( 1.0 - self.get_distance( s['x'], s['y'] ) )

            if DEBUG:
                print '%s :: %s' % ( self.base_url+'head', self.get_distance( s['x'], s['y'] ))

        # send message
        c.send( msg )

        self.move_master()

    def send_speakers( self ):
        msg = OSC.OSCMessage()
        msg.setAddress( self.base_url + 'speakers' )
        for i, skr in enumerate( self.speakers ):
            msg.append( int(skr['t'].get_state()) )

        # send message
        c.send( msg )


    def send_inputs( self ):
        msg = OSC.OSCMessage()
        msg.setAddress( self.base_url + 'inputs' )
        for i, inp in enumerate( self.inputs ):
            msg.append( int(inp.get_state()) )

        # send message
        c.send( msg )


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

    # TODO get rid of it
    def walk_and_stop( self ):
        if self.walker.is_alive():
            # stop the walker and bring the head icon back
            self.walker.terminate()
            self.head = self.cv.create_rectangle( self.hx - 3, self.hy - 3,
                                                  self.hx + 3, self.hy + 3,
                                                  fill='#6bbb7b', outline='' )
        else:
            # start the walker
            self.walker = mp5.Process( target=Track.walk_around, args=( self, self.walker.is_alive() ) )
            self.walker.start()
            # hide the head icon
            self.cv.delete( self.head )


    def walk_around( self, onoff ):
        import time
        while True:
            event = {
                'x': self.hx + ( 2.5 - random.random() * 5 ),
                'y': self.hy + ( 2.5 - random.random() * 5 )
            }

            self.move_head( event )
            time.sleep( 0.02 )

    def move_head( self, event ):
        '''Callback for the head move'''
        # keep head inside the panel
        try:
            self.hx = self.constrain( event.x, 18, 112 )
            self.hy = self.constrain( event.y, 3, 97 )
        except AttributeError:
            self.hx = self.constrain( event['x'], 18, 112 )
            self.hy = self.constrain( event['y'], 3, 97 )

        # move it to a new position
        self.cv.coords( self.head, self.hx-3, self.hy-3, self.hx+3, self.hy+3 )

        # send OSC information about new distance from each speaker
        # prepare message
        msg = OSC.OSCMessage()
        msg.setAddress( self.base_url + 'head' )
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
        self.cback= self.pop( options, 'cback', (lambda: None))


    def set_state( self, state ):
        self.on = not bool( state )
        self.toggle()

    def get_state( self ):
        return self.on

    def send_state( self, url='/no_url_provided' ):
        msg = OSC.OSCMessage()
        msg.setAddress( url )
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

        self.cback()

        return self.on

class StateManager:
    def __init__( self, master, tracks ):
        self.frame = Frame( master, **defaults )
        self.frame['pady'] = 15
        self.frame.pack( )#side=LEFT )

        self.tracks = tracks

        try:
            self.scenes = pickle.load( open('scenes.db', 'rb') )
            for t, s in zip( tracks, self.scenes[0] ):
                t.set_state( s )
        except:
            # initialize scenes with a current init state
            self.scenes = [ [ e.get_state() for e in tracks ] ]

        self.current_scene = 0

        # scenes switch canvas
        opts = {
            'width'  : 500,
            'height' : 15,
        }
        opts.update( defaults )

        self.switch_cv = Canvas( self.frame, **opts )
        self.switch_cv.pack()

        self.switch_cv.bind( '<Button-1>', self.click )
        if DEBUG:
            for t in tracks:
                t.send_state()

        self.render_scenes_switch( 0 )

        prev_scene = Button( master, **defaults )
        prev_scene['fg'] = '#9b9b9b'
        prev_scene['bg'] = '#363636'
        prev_scene['text'] = '<<'
        prev_scene['command'] = self.prev_scene
        prev_scene.pack( side=LEFT )

        next_scene = Button( master, **defaults )
        next_scene['fg'] = '#9b9b9b'
        next_scene['bg'] = '#363636'
        next_scene['text'] = '>>'
        next_scene['command'] = self.next_scene
        next_scene.pack( side=LEFT )

        # separator
        opts = { 'width': 5 }
        opts.update( defaults )
        Frame( master, **opts ).pack( side=LEFT )

        changes = Button( master, **defaults )
        changes['fg'] = '#9b9b9b'
        changes['bg'] = '#363636'
        changes['text'] = 'SAVE CHANGES'
        changes['command'] = self.save_state
        changes.pack( side=LEFT )

        save = Button( master, **defaults )
        save['fg'] = '#9b9b9b'
        save['bg'] = '#363636'
        save['text'] = 'SAVE & NEW'
        save['command'] = self.save_state_and_new
        save.pack( side=LEFT, padx=1 )

        insert = Button( master, **defaults )
        insert['fg'] = '#9b9b9b'
        insert['bg'] = '#363636'
        insert['text'] = 'INSERT NEW'
        insert['command'] = self.insert_new
        insert.pack( side=LEFT, padx=1 )

        delete = Button( master, **defaults )
        delete['fg'] = '#9b9b9b'
        delete['bg'] = '#363636'
        delete['text'] = 'DELETE'
        delete['command'] = self.delete_scene
        delete.pack( side=LEFT )

        reset = Button( master, **defaults )
        reset['fg'] = '#9b9b9b'
        reset['bg'] = '#363636'
        reset['text'] = 'RESET'
        reset['command'] = self.restore_state
        reset.pack( side=LEFT, padx=2 )

    def render_scenes_switch( self, current_scene ):
        self.switch_cv.delete( ALL )
        for i in range( len( self.scenes) ):
            offset  = 16 * i
            f_color = '#8b8b8b' if i == current_scene else '#3b3b3b'
            self.switch_cv.create_rectangle( offset, 0,
                                             offset + 15, 15,
                                             fill=f_color, outline='' )

    def click( self, event ):
        self.current_scene = event.x / 16
        self.render_scenes_switch( self.current_scene )
        for t, s in zip( self.tracks, self.scenes[ self.current_scene ] ):
            t.set_state( s )

    def get_current_state( self ):
        return [ e.get_state() for e in self.tracks ]

    def save_state( self ):
        self.scenes[ self.current_scene ] = self.get_current_state()
        try:
            pickle.dump( self.scenes, open('scenes.db', 'wb') )

            if DEBUG:
                print "State save!"

        except Exception as e:
            print "-----------------"
            print ">> Can't save the state"
            print e
            print "-----------------"

    def insert_new( self ):
        off   = self.current_scene+1
        self.scenes = self.scenes[:off] + [ [] ] + self.scenes[off:]
        self.current_scene += 1

        self.reset_state()
        self.scenes[ self.current_scene ] = self.get_current_state()

        self.render_scenes_switch( self.current_scene )

    def delete_scene( self ):
        del self.scenes[ self.current_scene ]

        self.current_scene -= 1
        if self.current_scene < 0:
            self.current_scene = 0
        if len( self.scenes ) == 0:
            self.reset_state()
            self.scenes = [ self.get_current_state() ]

        self.render_scenes_switch( self.current_scene )
        for t, s in zip( self.tracks, self.scenes[ self.current_scene ] ):
            t.set_state( s )
        self.save_state()

    def save_state_and_new( self ):
        self.save_state()

        self.current_scene = len( self.scenes )
        self.reset_state()
        self.scenes.append( self.get_current_state() )

        self.render_scenes_switch( len( self.scenes ) - 1 )

    def reset_state( self ):
        map( Track.set_state, tracks )

    def restore_state( self ):
        for t, s in zip( self.tracks, self.scenes[ self.current_scene ] ):
            t.set_state( s )

    def next_scene( self ):
        self.current_scene += 1
        # if faces the end of the scenes, start from the beginning
        if self.current_scene == len( self.scenes ):
            self.current_scene = 0

        self.render_scenes_switch( self.current_scene )
        for t, s in zip( self.tracks, self.scenes[ self.current_scene ] ):
            t.set_state( s )

    def prev_scene( self ):
        self.current_scene -= 1
        # if faces the end of the scenes, start from the beginning
        if self.current_scene < 0:
            self.current_scene = len( self.scenes )-1

        self.render_scenes_switch( self.current_scene )
        for t, s in zip( self.tracks, self.scenes[ self.current_scene ] ):
            t.set_state( s )

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
