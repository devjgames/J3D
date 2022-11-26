#version 150

in vec2 coord1;
in vec2 coord2;
in vec2 coord3;

out vec4 oColor;

/// vec4 color1 = 1 1 0.5 1
uniform vec4 color1;
/// vec4 color2 = 0.5 0.25 0 1
uniform vec4 color2;
/// bool julia = false
uniform int julia;
/// vec2 seed = -0.75 0.15
uniform vec2 seed;
/// float factor = 1000
uniform float factor;
/// bool threeD = false
uniform int threeD;
/// float height = 1
uniform float height;
/// vec3 lightDirection = 0 -1 -1
uniform vec3 lightDirection;

int i = 0;

float dist(vec2 z, vec2 c) {
    vec2 d = vec2(1.0, 0.0);
    vec2 zn;
    vec2 adder =  vec2(1.0, 0.0);

    if(julia != 0) {
        z = c;
        c = seed;
        adder = vec2(0.0, 0.0);
    }
    for(i = 0; i != 1000; i++) {
        zn = vec2(z.x * z.x - z.y * z.y + c.x, z.x * z.y + z.x * z.y + c.y);
        d = 2.0 * vec2(z.x * d.x - z.y * d.y, z.x * d.y + d.x * z.y) + adder;
        z = zn;
        if(length(z) > 1000.0) {
            break;
        }
    }
    return length(z) * log(length(z)) / length(d);
}

void main() {
    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);

    if(threeD != 0) {
        vec2 z1 = vec2(0.0, 0.0);
        vec2 z2 = vec2(0.0, 0.0);
        vec2 z3 = vec2(0.0, 0.0);
        float d1 = dist(z1, coord1);
        float d2 = dist(z2, coord2);
        float d3 = dist(z3, coord3);

        if(i < 1000) {
            vec3 p1 = vec3(coord1, (6.0 - d1) * height);
            vec3 p2 = vec3(coord2, (6.0 - d2) * height);
            vec3 p3 = vec3(coord3, (6.0 - d3) * height);
            vec3 n = normalize(cross(p1 - p2, p2 - p3));
            vec3 l = normalize(-lightDirection);
            vec3 c = mix(color2, color1, clamp(d1 * factor, 0.0, 1.0)).rgb;

            color.rgb = c * (0.2 + clamp(dot(l, n), 0.0, 1.0));
        }
    } else {
        float d = dist(vec2(0.0, 0.0), coord1);

        if(i < 1000) {
            color = mix(color2, color1, clamp(d * factor, 0.0, 1.0));
        }
    }
    oColor = color;
}