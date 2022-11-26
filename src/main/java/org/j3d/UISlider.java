package org.j3d;

class UISlider extends UIControl {

    private final String label;
    private final int cols;
    private Float changed = null;
    private float value = 0;
    private boolean drag = false;
    
    public UISlider(UIManager manager, String label, int cols) {
        super(manager);

        this.label = label;
        this.cols =cols;
    }

    @Override
    public int getWidth() {
        return UIManager.gap * 2 + cols * getManager().getFont().getCharWidth();
    }

    @Override
    public int getHeight() {
        return getManager().getFont().getCharHeight() * 2 + UIManager.gap * 3;
    }

    public void setValue(float value) {
        this.value = Math.max(0, Math.min(1, value));
    }

    public Float getChanged() {
        Float c = changed;

        changed = null;

        return c;
    }

    private int getThumbX() {
        return getX() + UIManager.gap + (int)(value * (getSliderLength() - getThumbSize()));
    }

    private int getThumbSize() {
        return getManager().getFont().getCharHeight();
    }

    private int getThumbY() {
        int ch = getManager().getFont().getCharHeight();

        return getY() + UIManager.gap + ch + UIManager.gap;
    }

    private int getSliderY() {
        int ch = getManager().getFont().getCharHeight();

        return getThumbY() + ch / 2;
    }

    private int getSliderLength() {
        return getWidth() - UIManager.gap * 2;
    }

    @Override
    public void pushRects() {
        int x = getThumbX();
        int y = getThumbY();
        int l = getSliderLength();
        int s = getThumbSize();

        getManager().pushRect(getX(), getY(), getWidth(), getHeight());
        getManager().pushRect(getX() + UIManager.gap, getSliderY(), l, 1, UIManager.foregroundColor);
        getManager().pushRect(x, y, s, s);
    }

    @Override
    public void pushText() {
        int x = getX() + UIManager.gap;
        int y = getY() + UIManager.gap;

        getManager().pushText(label, x, y, cols, UIManager.selectionColor);
    }

    @Override
    public void mouseDown(int x, int y) {
        int tx = getThumbX();
        int ty = getThumbY();
        int ts = getThumbSize();

        if(x >= tx && x <= tx + ts && y >= ty && y <= ty + ts) {
            drag = true;
            setValue(x);
        }
    }

    @Override
    public void mouseMove(int x, int y) {
        if(drag) {
            setValue(x);
        }
    }

    @Override
    public void mouseUp(int x, int y) {
        drag = false;
    }

    private void setValue(int x) {
        int s = getX() + UIManager.gap;

        value = Math.max(0, Math.min(1, (x - s) / (float)(getSliderLength() - getThumbSize())));
        changed = value;
    }

    @Override
    public void end() {
        changed = null;
    }
}
