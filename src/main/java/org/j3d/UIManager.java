package org.j3d;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

public class UIManager {

    public static final Vector4f backgroundColor = new Vector4f(0.1f, 0.1f, 0.1f, 1);
    public static final Vector4f foregroundColor = new Vector4f(0.3f, 0.3f, 0.3f, 1);
    public static final Vector4f selectionColor = new Vector4f(1, 1, 1, 1);
    public static int gap = 8;
    
    private final Game game;
    private final Font font;
    private final Hashtable<String, UIControl> controls = new Hashtable<>();
    private final Vector<UIControl> stack = new Vector<>();
    private UIControl activeControl;
    private boolean handled = false;
    private int x = 0;
    private int y = 0;
    private int startX = 0;
    private int maxH = 0;

    public UIManager(Game game, Font font) throws Exception {
        this.game = game;
        this.font = font;

        GLFW.glfwSetKeyCallback(game.getWindow(), (window, key, scancode, action, mods) -> handleKey(key, scancode, action, mods));
        GLFW.glfwSetCharCallback(game.getWindow(), (window, codepoint) -> handleChar(codepoint));
        GLFW.glfwSetMouseButtonCallback(game.getWindow(), (window, button, action, mods) -> handleMouseButton(button, action, mods));
    }

    public Game getGame() {
        return game;
    }

    public Font getFont() {
        return font;
    }

    public String removeBreakingWhiteSpace(String text) {
        text = text.replace("\t", " ").replace("\n", " ");
        return text;
    }

    public void pushRect(int x,  int y, int w, int h) {
        pushRect(x, y, w, h, foregroundColor);
        pushRect(x + 1, y + 1, w - 2, h - 2, backgroundColor);
    }

    public void pushRect(int x, int y, int w, int h, Vector4f color) {
        game.getSpritePipeline().push(font.getWhiteX(), font.getWhiteY(), 1, 1, x, y, w, h, color.x, color.y, color.z, color.w, false);
    }

    public void pushText(String text, int x, int y, int maxChars, Vector4f color) {
        if(text.length() > maxChars) {
            text = text.substring(0, maxChars);
        }
        game.getSpritePipeline().push(font, text, 0, x, y, color.x, color.y, color.z, color.w);
    }

    public void begin() {
        Enumeration<String> keys = controls.keys();

        while(keys.hasMoreElements()) {
            UIControl control = controls.get(keys.nextElement());

            control.setVisible(false);
        }
    }

    protected UIControl getControl(String key) {
        return controls.get(key);
    }

    protected void addControl(String key, UIControl control) {
        controls.put(key, control);
        stack.add(control);
    }

    protected void locateControl(UIControl control, int gap) {
        x += gap;
        control.setLocation(x, y);
        x += control.getWidth();
        maxH = Math.max(maxH, control.getHeight());
    }

    public void moveTo(int x, int y) {
        this.startX = x;
        this.x = x;
        this.y = y;
        this.maxH = 0;
    }

    public void addRow(int gap) {
        x = startX;
        y += maxH + gap;
        maxH = 0;
    }

    public boolean label(String key, int gap, String title, int maxChars, boolean selected) {
        UILabel label = (UILabel)getControl(key);

        if(label == null) {
            label = new UILabel(this, (maxChars <= 0) ? title.length() : maxChars);
            addControl(key, label);
        }
        label.setTitle(title);
        label.setVisible(true);
        label.selected = selected;
        locateControl(label, gap);

        return label.getClicked();
    }

    public String textField(String key, int gap, String label, String text, boolean reset, int cols) {
        UITextField textField = (UITextField)getControl(key);

        if(textField == null) {
            textField = new UITextField(this, label, cols);
            textField.setText(text);
            addControl(key, textField);
        }
        textField.setVisible(true);
        if(reset) {
            textField.setText(text);
        }
        locateControl(textField, gap);

        return textField.getChanged();
    }

    public Integer list(String key, int gap, String[] items, int cols, int rows, int select) {
        UIList list = (UIList)getControl(key);

        if(list == null) {
            list = new UIList(this, cols, rows);
            addControl(key, list);
        }
        list.setVisible(true);
        list.setItems(items);
        list.select(select);
        locateControl(list, gap);

        return list.getChanged();
    }

    public Integer list(String key, int gap, Vector<String> items, int cols, int rows, int select) {
        UIList list = (UIList)getControl(key);

        if(list == null) {
            list = new UIList(this, cols, rows);
            addControl(key, list);
        }
        list.setVisible(true);
        list.setItems(items);
        list.select(select);
        locateControl(list, gap);

        return list.getChanged();
    }

    public Float slider(String key, int gap, String label, float value, int cols, boolean reset) {
        UISlider slider = (UISlider)getControl(key);

        if(slider == null) {
            slider = new UISlider(this, label, cols);
            slider.setValue(value);
            addControl(key, slider);
        }
        slider.setVisible(true);
        locateControl(slider, gap);
        if(reset) {
            slider.setValue(value);
        }

        return slider.getChanged();
    }

    public boolean end() {
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        for(UIControl control : stack) {
            if(control.isVisible()) {
                game.getSpritePipeline().beginSprite(font);
                control.pushRects();
                control.pushText();
                game.getSpritePipeline().endSprite();
                control.pushImages();
            }
        }
        game.getSpritePipeline().end();

        int x = game.getMouseX();
        int y = game.getMouseY();

        if(activeControl != null) {
            activeControl.mouseMove(x, y);
            activeControl.update();
        }
        return handled;
    }

    private void handleKey(int key, int scancode, int action, int mods) {
        if(activeControl != null) {
            if(action == GLFW.GLFW_PRESS) {
                activeControl.keyDown(key);
            } else {
                activeControl.keyUp(key);
            }
        }
    }

    private void handleChar(int codepoint) {
        if(activeControl != null) {
            activeControl.charDown(codepoint);
        }
    }

    private void handleMouseButton(int button, int action, int mods) {
        int x = game.getMouseX();
        int y = game.getMouseY();

        if(action == GLFW.GLFW_PRESS) {
            UIControl control = null;

            for(UIControl iControl : stack) {
                if(iControl.hitTest(x, y)) {
                    control = iControl;
                }
            }
            if(control != null) {
                if(control == activeControl) {
                    control.mouseDown(x, y);
                } else {
                    deactivate();
                    activeControl = control;
                    activeControl.mouseDown(x, y);
                }
                handled = true;
            } 
        } else {
            if(activeControl != null) {
                activeControl.mouseUp(x, y);
                if(activeControl.deactivateOnMouseUp()) {
                    deactivate();
                }
            }
            handled = false;
        }
    }

    private void deactivate() {
        if(activeControl != null) {
            activeControl.deactivate();
            activeControl = null;
        }
    }
}
