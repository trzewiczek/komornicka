class Button {

    public Button( int ux, int uy, int ua ) {
        x = ux;
        y = uy;
        a = ua;
    }

    public boolean is_hit() {
        if( mouseX > x && mouseX < x+a && 
            mouseY > y && mouseY < y+a ) {

            return true;
        }
        else {
            return false;
        }
    }
    
    protected int x;
    protected int y;
    protected int a;
}

