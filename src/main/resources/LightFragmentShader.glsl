#version 150

#define MAX_LIGHTS 6

in vec3 fsInPosition;
in vec2 fsInTextureCoordinate;
in vec3 fsInNormal;
in vec3 fsInColor;

out vec4 oColor;

uniform vec3 uLightVector[MAX_LIGHTS];
uniform vec4 uLightColor[MAX_LIGHTS];
uniform float uLightRadius[MAX_LIGHTS];
uniform int uLightDirectional[MAX_LIGHTS];

uniform int uLightCount;

uniform vec4 uAmbientColor;
uniform vec4 uDiffuseColor;

uniform int uVertexColorEnabled;
uniform float uAmbientFactor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

uniform sampler2D uDecal;
uniform int uDecalEnabled;

void main() {
    vec4 color = uAmbientColor;
    vec3 normal = normalize(fsInNormal);
    vec3 position = fsInPosition;
    vec4 diffuseColor = uDiffuseColor;

    if(uVertexColorEnabled != 0) {
        color = vec4(fsInColor, color.a) * uAmbientFactor;
        diffuseColor = vec4(fsInColor, diffuseColor.a);
    }

    for(int i = 0; i != uLightCount; i++) {
        if(uLightDirectional[i] != 0) {
            float lDotN = clamp(dot(normal, normalize(-uLightVector[i])), 0.0, 1.0);

            color += lDotN * uDiffuseColor * uLightColor[i];
        } else {
            vec3 offset = uLightVector[i] - position;
            vec3 lNormal = normalize(offset);
            float lDotN = clamp(dot(normal, lNormal), 0.0, 1.0);
            float atten = 1.0 - clamp(length(offset) / uLightRadius[i], 0.0, 1.0);

            color += atten * lDotN * diffuseColor * uLightColor[i];
        }
    }
    if(uTextureEnabled != 0) {
        color *= texture(uTexture, fsInTextureCoordinate);
    }
    if(uDecalEnabled != 0) {
        vec4 decal = texture(uDecal, fsInTextureCoordinate);

        color.rgb = (1.0 - decal.a) * color.rgb + decal.a * decal.rgb;
    }
    oColor = color;
}