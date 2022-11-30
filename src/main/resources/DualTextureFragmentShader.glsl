#version 150

in vec2 fsInTextureCoordinate;
in vec2 fsInTextureCoordinate2;

out vec4 oColor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

uniform sampler2D uTexture2;
uniform int uTexture2Enabled;

uniform vec4 uColor;

uniform vec4 uEmissiveColor;
uniform int uEmissiveColorEnabled;

void main() {
    vec4 color = uColor;
    
    if(uTextureEnabled != 0) {
        color *= texture(uTexture, fsInTextureCoordinate);
    }
    if(uEmissiveColorEnabled != 0) {
        color *= uEmissiveColor;
    } else if(uTexture2Enabled != 0) {
        color *= texture(uTexture2, fsInTextureCoordinate2);
    }
    oColor = color;
 }