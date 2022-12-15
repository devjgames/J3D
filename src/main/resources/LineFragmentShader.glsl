#version 150

in vec3 fsInColor;

out vec4 oColor;

void main() {
    oColor = vec4(fsInColor, 1.0);
}