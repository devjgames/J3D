#version 150

#define MAX_LIGHTS 8

in vec3 fsInPosition;
in vec3 fsInNormal;

out vec4 oColor;

uniform vec3 uLightPosition[MAX_LIGHTS];
uniform vec3 uLightColor[MAX_LIGHTS];
uniform float uLightRadius[MAX_LIGHTS];

uniform int uLightCount;

uniform vec4 uAmbientColor;
uniform vec4 uDiffuseColor;

void main() {
    vec4 color = uAmbientColor;
    vec3 position = fsInPosition;
    vec3 normal = normalize(fsInNormal);

    for(int i = 0; i != uLightCount; i++) {
        vec3 offset = uLightPosition[i] - position;
        vec3 lightNormal = normalize(offset);
        float lDotN = clamp(dot(lightNormal, normal), 0.0, 1.0);
        float atten = 1.0 - clamp(length(offset) / uLightRadius[i], 0.0, 1.0);

        color += atten * lDotN * uDiffuseColor * vec4(uLightColor[i], 1.0);
    }
    oColor = color;
}

