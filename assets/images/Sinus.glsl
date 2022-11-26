#version 150

in vec2 coord;

out vec4 oColor;

/// vec4 color1 = 1 0.5 0 1
uniform vec4 color1;
/// vec4 color2 = 1 1 1 1
uniform vec4 color2;
/// vec4 color3 = 0.2 0 0.2 1
uniform vec4 color3;
/// float frequency1 = 12
uniform float frequency1;
/// float frequency2 = 6
uniform float frequency2;
/// float frequency3 = 2.5
uniform float frequency3;

void main() {
    float x = sin((cos(coord.x * frequency1) * 0.5 + 0.5) + (sin(coord.y * frequency2) * 0.5 + 0.5) * frequency3) * 0.5 + 0.5;

    if(x < 0.33) {
        x /= 0.33;
        oColor = mix(color1, color2, x);
    } else if(x < 0.66) {
        x = (x - 0.33) / 0.33;
        oColor = mix(color2, color3, x);
    } else {
        x = (x - 0.66) / 0.33;
        oColor = mix(color3, color1, x);
    }
}