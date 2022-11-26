package org.j3d;

import java.util.Vector;

class UIList extends UIControl {

    private final Vector<String> items = new Vector<>();
    private Integer changed = null;
    private final int cols;
    private final int rows;
    private int start = 0;
    private int thumbSize = 0;
    private int thumbPosition = 0;
    private int selectedIndex = -1;
    private boolean drag = false;
    private final int cw, ch;

    public UIList(UIManager manager, int cols, int rows) {
        super(manager);

        this.cols = cols;
        this.rows = rows;

        cw = manager.getFont().getCharWidth();
        ch = manager.getFont().getCharHeight();
    }

    @Override
    public int getWidth() {
        return cols * cw + UIManager.gap * 2 + 16;
    }

    @Override
    public int getHeight() {
        return rows * (ch + 4) + UIManager.gap * 2;
    }
    
    public Integer getChanged() {
        Integer c = changed;

        changed = null;

        return c;
    }

    public void select(int index) {
        if(index >= -1 && index < items.size()) {
            selectedIndex = index;
            changed = null;
        }
    }

    public String getSelectedItem() {
        if(selectedIndex != -1) {
            return items.get(selectedIndex);
        }
        return null;
    }

    public void setItems(Vector<String> items) {
        boolean update = items.size() != this.items.size();

        if(!update) {
            for(int i = 0; i != items.size(); i++) {
                if(!items.get(i).equals(this.items.get(i))) {
                    update = true;
                    break;
                }
            }
        }
        if(update) {
            this.items.removeAllElements();
            this.items.addAll(items);
            start = 0;
            selectedIndex = -1;
            changed = null;
            calcThumb();
        }
    }

    public void setItems(String[] items) {
        boolean update = items.length != this.items.size();

        if(!update) {
            for(int i = 0; i != items.length; i++) {
                if(!items[i].equals(this.items.get(i))) {
                    update = true;
                    break;
                }
            }
        }
        if(update) {
            this.items.removeAllElements();
            for(String item : items) {
                this.items.add(item);
            }
            start = 0;
            selectedIndex = -1;
            changed = null;
            calcThumb();
        }
    }

    @Override
    public void pushRects() {
        getManager().pushRect(getX(), getY(), getWidth(), getHeight());
        if(thumbSize > 0) {
            getManager().pushRect(
                getX() + (getWidth() - 16), getY() + thumbPosition + 4, 12, thumbSize - 8, 
                UIManager.foregroundColor
                );
        }
    }

    @Override
    public void pushText() {
        for(int i = 0; i != rows; i++) {
            int j = start + i;
            if(j >= items.size()) {
                break;
            }
            String item = items.get(j);
            item = getManager().removeBreakingWhiteSpace(item);
            if(j == selectedIndex) {
                getManager().pushText(
                    item, 
                    getX() + UIManager.gap, 
                    getY() + UIManager.gap + i * (ch + 4) + 2, 
                    cols, 
                    UIManager.selectionColor
                    );
            } else {
                getManager().pushText(
                    item, 
                    getX() + UIManager.gap, 
                    getY() + UIManager.gap + i * (ch + 4) + 2, 
                    cols, 
                    UIManager.foregroundColor
                    );
            }
        }
    }

    @Override
    public void mouseDown(int x, int y) {
        if(
            x >= getX() + getWidth() - 16 &&  
            x <= getX() + getWidth() &&
            y >= getY() + UIManager.gap &&  
            y <= getY() + getHeight() - UIManager.gap && 
            thumbSize > 0) {
            drag = true;
        } else {
            for(int i = 0; i != rows; i++) {
                int j = start + i;
                if(j >= items.size()) {
                    break;
                }
                if(
                    x >= getX() + UIManager.gap && 
                    x <= getX() + getWidth() - 16 && 
                    y >= getY() + UIManager.gap + i * (ch + 4) && 
                    y <= getY() + UIManager.gap + i * (ch + 4) + ch + 4) {
                    if(selectedIndex != j) {
                        selectedIndex = j;
                        changed = j;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void mouseUp(int x, int y) {
        drag = false;
    }

    @Override
    public void mouseMove(int x, int y) {
        if(drag) {
            thumbPosition = y - getY();
            thumbPosition = Math.max(0, thumbPosition);
            thumbPosition = Math.min(getHeight() - thumbSize, thumbPosition);
            start = (int)(thumbPosition / (float)(getHeight() - thumbSize) * (items.size() - rows));
            start = Math.max(0, start);
            start = Math.min(items.size() - rows, start);
        }
    }

    @Override
    public void end() {
        changed = null;
    }
    
    private void calcThumb() {
        if(items.size() > rows) {
            thumbSize = (int)((rows / (float)items.size()) * getHeight());
            thumbSize = Math.max(16, thumbSize);
            thumbPosition = (int)(start / (float)items.size() - rows) * (getHeight() + thumbSize);
            thumbPosition = Math.max(0, thumbPosition);
            thumbPosition = Math.min(getHeight() - thumbSize, thumbPosition);
        } else {
            thumbSize = 0;
            thumbPosition = 0;
        }
    }
}
