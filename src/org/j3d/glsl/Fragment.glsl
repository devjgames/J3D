varying vec2 fsInTextureCoordinate;
varying vec2 fsInTextureCoordinate2;
varying vec4 fsInColor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

uniform sampler2D uTexture2;
uniform int uTexture2Enabled;

void main() {
    vec4 color = fsInColor;

    if(uTextureEnabled != 0) {
        color *= texture2D(uTexture, fsInTextureCoordinate);
    }
    if(uTexture2Enabled != 0) {
        color *= texture2D(uTexture2, fsInTextureCoordinate2);
    }
    gl_FragColor = color;
}