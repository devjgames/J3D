#version 150

in vec2 fsInTextureCoordinate;
in float fsInTextureIndex;

out vec4 oColor;

uniform sampler2D uTexture0;
uniform sampler2D uTexture1;
uniform sampler2D uTexture2;
uniform sampler2D uTexture3;
uniform sampler2D uTexture4;
uniform sampler2D uTexture5;

uniform vec4 uColor;

void main() {
    vec4 color = uColor;
    int i = int(fsInTextureIndex);
    
    if(i == 0) {
        color *= texture(uTexture0, fsInTextureCoordinate);
    } else if(i == 1) {
        color *= texture(uTexture1, fsInTextureCoordinate);
    } else if(i == 2) {
        color *= texture(uTexture2, fsInTextureCoordinate);
    } else if(i == 3) {
        color *= texture(uTexture3, fsInTextureCoordinate);
    } else if(i == 4) {
        color *= texture(uTexture4, fsInTextureCoordinate);
    } else if(i == 5) {
        color *= texture(uTexture5, fsInTextureCoordinate);
    }
    oColor = color;
}