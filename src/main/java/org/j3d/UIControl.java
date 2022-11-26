package org.j3d;

public abstract class UIControl {

    private UIManager manager;
    private int x = 0;
    private int y = 0;
    private boolean visible = true;
    
    public UIControl(UIManager manager) {
        this.manager = manager;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public UIManager getManager() {
        return manager;
    }

    public Game getGame() {
        return getManager().getGame();
    }

    public SpritePipeline getSpritePipeline() {
        return getGame().getSpritePipeline();
    }

    public void pushRects() {
    }

    public void pushText() {
    }

    public void pushImages() {
    }

    public void mouseDown(int x, int y) {
    }

    public void mouseUp(int x, int y) {
    }

    public void mouseMove(int x, int y) {
    }

    public void keyDown(int key) {
    }

    public void keyUp(int key) {
    }

    public void charDown(int ch) {
    }

    public void update() {
    }

    public void deactivate() {
    }

    public void end() {
    }

    public boolean deactivateOnMouseUp() {
        return true;
    }

    public boolean hitTest(int x, int y) {
        if(isVisible()) {
            int w = getWidth();
            int h = getHeight();
            int x1 = this.x;
            int y1 = this.y;
            int x2 = x1 + w;
            int y2 = y1 + h;

            return x >= x1 && x < x2 && y >= y1 && y <= y2;
        }
        return false;
    }
}
