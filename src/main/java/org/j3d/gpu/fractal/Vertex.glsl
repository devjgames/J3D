#version 150

in vec2 vsInPosition;

out vec2 coord1;
out vec2 coord2;
out vec2 coord3;

uniform vec2 uMin;
uniform vec2 uMax;
uniform vec2 uPixelSize;

void main() {
    gl_Position = vec4(vsInPosition, 0.0, 1.0);
    coord1 = uMin + (vsInPosition * 0.5 + 0.5) * (uMax - uMin);
    coord2 = uMin + (vsInPosition * 0.5 + 0.5 + vec2(+1.0, +0.0) * uPixelSize) * (uMax - uMin);
    coord3 = uMin + (vsInPosition * 0.5 + 0.5 + vec2(+0.0, +1.0) * uPixelSize) * (uMax - uMin);
}