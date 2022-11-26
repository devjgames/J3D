#version 150

in vec2 coord;

out vec4 oColor;

/// vec4 lineColor = 0.2 0 0.2 1
uniform vec4 lineColor;
/// vec4 color1 = 1 0.5 0 1
uniform vec4 color1;
/// vec4 color2 = 1 1 0 1
uniform vec4 color2;
/// vec4 backgroundColor = 0 0 0 1
uniform vec4 backgroundColor;
/// float radius = 0.4
uniform float radius;
/// float lineWidth = 0.05
uniform float lineWidth;

void main() {
    float d = length(coord);
    vec4 color = backgroundColor;

    if(d < radius + lineWidth) {
        if(d > radius) {
            color = lineColor;
        } else {
            color = mix(color2, color1, d / radius);
        }
    }
    oColor = color;
}