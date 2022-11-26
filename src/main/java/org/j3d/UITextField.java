package org.j3d;

import org.lwjgl.glfw.GLFW;

class UITextField extends UIControl {

    private String text = "";
    private String label = "";
    private final int cols;
    private int start = 0;
    private int cursor = 0;
    private String changed = null;
    private float seconds = 0;
    private boolean drawCursor = false;
    private final int cw, ch;
    
    public UITextField(UIManager manager, String label, int cols) {
        super(manager);

        this.cols = cols;
        this.label = label;

        cw = manager.getFont().getCharWidth();
        ch = manager.getFont().getCharHeight();
    }

    @Override
    public int getWidth() {
        return cols * cw + UIManager.gap * 2;
    }

    @Override
    public int getHeight() {
        return ch * 2 + UIManager.gap * 3;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = getManager().removeBreakingWhiteSpace(text);
        start = 0;
        cursor = 0;
    }

    public String getChanged() {
        String c = changed;
        changed = null;
        return c;
    }

    @Override
    public void pushRects() {
        int x = getX();
        int y = getY();

        getManager().pushRect(x, y,getWidth(), getHeight());

        if(drawCursor) {
            x += UIManager.gap + cursor * cw;
            y += UIManager.gap * 2 + ch;
            getManager().pushRect(x, y, cw, ch, UIManager.foregroundColor);
        }
    }

    @Override
    public void pushText() {
        int x = getX() + UIManager.gap;
        int y = getY() + UIManager.gap;

        getManager().pushText(label, x, y, cols, UIManager.selectionColor);
        getManager().pushText(text.substring(start, Math.min(text.length(), start + cols)), x, y + UIManager.gap + ch, cols, UIManager.foregroundColor);
        if(drawCursor) {
            int i = start + cursor;
            if(i < text.length()) {
                x += cursor * cw;
                getManager().pushText("" + text.charAt(i), x, y + UIManager.gap + ch, 1, UIManager.backgroundColor);
            }
        }
    }

    @Override
    public void deactivate() {
        drawCursor = false;
    }

    @Override
    public boolean deactivateOnMouseUp() {
        return false;
    }

    @Override
    public void mouseDown(int x, int y) {
        int x1 = getX() + UIManager.gap;
        int y1 = getY() + UIManager.gap * 2 + ch;
        int x2 = x1 + cw * cols;
        int y2 = y1 + ch;

        if(x >= x1 && x <= x2 && y >= y1 && y <= y2) {
            int i = start + (x - getX() - UIManager.gap) / cw;
            if(i < 0) {
                i = 0;
            } else if(i > text.length()) {
                i = text.length();
            }
            cursor = Math.min(cols - 1, i - start);
            seconds = 0;
            drawCursor = true;
        }
    }

    @Override
    public void keyDown(int key) {
        if(key == GLFW.GLFW_KEY_BACKSPACE) {
            if(!text.isEmpty()) {
                if(text.length() == 1) {
                    text = "";
                } else {
                    int i = start + cursor;
                    if(i == 0) {
                        text = text.substring(1);
                    } else if(i >= text.length()) {
                        text = text.substring(0, text.length() - 1);
                    } else {
                        text = text.substring(0, i - 1) + text.substring(i);
                    }
                }
                decCursor();
                changed = text;
            }
        } else if(key == GLFW.GLFW_KEY_LEFT) {
            decCursor();
        } else if(key == GLFW.GLFW_KEY_RIGHT) {
            incCursor();
        } else {
            return;
        }
        seconds = 0;
        drawCursor = true;
    }

    @Override
    public void charDown(int c) {
        if(c >= 32 && c < 128) {
            char ch = (char)c;
            if(text.isEmpty()) {
                text += ch;
            } else {
                int i = start + cursor;
                if(i == 0) {
                    text = ch + text;
                } else if(i >= text.length()) {
                    text += ch;
                } else {
                    text = text.substring(0, i) + ch + text.substring(i);
                }
            }
            changed = text;
            incCursor();
            seconds = 0;
            drawCursor = true;
        }
    }

    @Override
    public void update() {
        seconds += getGame().getElapsedTime();
        if(seconds >= 1) {
            drawCursor = !drawCursor;
            seconds = 0;
        }
    }

    @Override
    public void end() {
        changed = null;
    }

    private void decCursor() {
        cursor--;
        if(cursor < 0) {
            start--;
            if(start < 0) {
                start = 0;
            }
            cursor = 0;
        }
    }

    private void incCursor() {
        cursor++;
        if(start + cursor > text.length()) {
            cursor--;
        } else if(cursor >= cols) {
            start++;
            cursor--;
        }
    }
}
