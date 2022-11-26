#version 150

#define MAX_LIGHTS 3

in vec3 fsInPosition;
in vec2 fsInTextureCoordinate;
in vec3 fsInNormal;

out vec4 oColor;

uniform vec3 uLightPosition[MAX_LIGHTS];
uniform vec3 uLightColor[MAX_LIGHTS];
uniform float uLightRadius[MAX_LIGHTS];

uniform int uLightCount;

uniform vec4 uAmbientColor;
uniform vec4 uDiffuseColor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

void main() {
    vec4 color = uAmbientColor;
    vec3 normal = normalize(fsInNormal);

    for(int i = 0; i < uLightCount; i++) {
        vec3 offset = uLightPosition[i] - fsInPosition;
        vec3 lNormal = normalize(offset);
        float dI = clamp(dot(lNormal, normal), 0.0, 1.0);
        float atten = 1.0 - clamp(length(offset) / uLightRadius[i], 0.0, 1.0);

        color += atten * dI * uDiffuseColor * vec4(uLightColor[i], 1.0);
    }
    if(uTextureEnabled != 0) {
        color *= texture(uTexture, fsInTextureCoordinate);

    }
    oColor = color;
}