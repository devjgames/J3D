attribute vec3 vsInPosition;
attribute vec2 vsInTextureCoordinate;
attribute vec2 vsInTextureCoordinate2;
attribute vec4 vsInColor;

varying vec2 fsInTextureCoordinate;
varying vec2 fsInTextureCoordinate2;
varying vec4 fsInColor;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
    gl_Position = uProjection * uView * uModel * vec4(vsInPosition, 1.0);

    fsInTextureCoordinate = vsInTextureCoordinate;
    fsInTextureCoordinate2 = vsInTextureCoordinate2;
    fsInColor = vsInColor;
}