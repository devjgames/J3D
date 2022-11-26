#version 150

in vec2 vsInPosition;

out vec2 coord;

uniform vec2 uMin;
uniform vec2 uMax;

void main() {
    gl_Position = vec4(vsInPosition, 0.0, 1.0);
    coord = uMin + (vsInPosition * 0.5 + 0.5) * (uMax - uMin);
}