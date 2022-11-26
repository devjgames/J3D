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
/// float width = 0.2
uniform float width;
/// float lineWidth = 0.05
uniform float lineWidth;
/// vec2 start = -0.4 -0.4
uniform vec2 start;
/// vec2 end = 0.4 0.4
uniform vec2 end;

float distance(vec2 point, vec2 a, vec2 b, float w) {
    vec2 ab = b - a;
    vec2 ap = point - a;
    vec2 c = a;
    float t = dot(ab, ap);

    if(t > 0.0) {
        t /= dot(ab, ab);
        if(t < 1.0) {
            c = a + t * ab;
        } else {
            c = b;
        }
    }
    float d = length(point - c);

    if(d < w) {
        return d;
    }
    return -2.0;
}

void main() {
    float d = distance(coord, start, end, width + lineWidth);
    vec4 color = backgroundColor;

    if(d > -1.0) {
        if(d > width) {
            color = lineColor;
        } else {
            color = mix(color2, color1, d / width);
        }
    }
    oColor = color;
}