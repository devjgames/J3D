package org.j3d;

import org.joml.Vector4f;

class UILabel extends UIControl {
    
    public boolean selected = false;

    private String title = "";
    private final int maxChars;
    private boolean clicked = false;
    private boolean down = false;
    private final int cw, ch;

    public UILabel(UIManager manager, int maxChars) {
        super(manager);

        this.maxChars = maxChars;

        cw = manager.getFont().getCharWidth();
        ch = manager.getFont().getCharHeight();
    }

    public boolean getClicked() {
        boolean c = clicked;

        clicked = false;

        return c;
    }

    @Override
    public int getWidth() {
        return cw * maxChars + UIManager.gap * 2 + 2;
    }

    @Override
    public int getHeight() {
        return ch + UIManager.gap * 2 + 2;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = getManager().removeBreakingWhiteSpace(title);
    }

    @Override
    public void pushRects() {
        getManager().pushRect(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void pushText() {
        Vector4f color = (selected) ? UIManager.selectionColor : UIManager.foregroundColor;
        
        if(down) {
            color = (color == UIManager.selectionColor) ? UIManager.foregroundColor : UIManager.selectionColor;
        }
        getManager().pushText(
            title, 
            getX() + UIManager.gap, getY() + UIManager.gap, maxChars, 
            color
            );
    }

    @Override
    public void mouseDown(int x, int y) {
        down = true;
    }

    @Override
    public void mouseUp(int x, int y) {
        if(hitTest(x, y)) {
            clicked = true;
        }
        down = false;
    }
    
    @Override
    public void end() {
        clicked = false;
    }
}
