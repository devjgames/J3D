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
/// float radius = 0.5
uniform float radius;
/// float lineWidth = 0.05
uniform float lineWidth;
/// float width = 0.1
uniform float width;
/// float frequency = 12
uniform float frequency;
/// float amplitude = 0.025
uniform float amplitude;

void main() {
    vec2 n = normalize(coord);
    float t = acos(clamp(n.x, -1.0, 1.0));
    vec4 color = backgroundColor;

    if(n.y < 0) {
        t = 2.0 * atan(1.0) * 4.0 - t;
    }
    t *= frequency;

    vec2 xy = n * radius + vec2(cos(t), sin(t)) * amplitude;
    float d = length(xy - coord);

    if(d < width + lineWidth) {
        if(d > width) {
            color = lineColor;
        } else {
            color = mix(color2, color1, d / width);
        }
    }
    oColor = color;
}