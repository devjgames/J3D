#version 150

#define MAX_LIGHTS 3

in vec2 fsInTextureCoordinate;
in vec3 fsInNormal;

out vec4 oColor;

uniform vec3 uLightDirection[MAX_LIGHTS];
uniform vec4 uLightColor[MAX_LIGHTS];

uniform int uLightCount;

uniform vec4 uAmbientColor;
uniform vec4 uDiffuseColor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

void main() {
    vec4 color = uAmbientColor;
    vec3 normal = normalize(fsInNormal);

    for(int i = 0; i != uLightCount; i++) {
        float lDotN = clamp(dot(normal, normalize(-uLightDirection[i])), 0.0, 1.0);

        color += lDotN * uDiffuseColor * uLightColor[i];
    }
    if(uTextureEnabled != 0) {
        color *= texture(uTexture, fsInTextureCoordinate);
    }
    oColor = color;
}