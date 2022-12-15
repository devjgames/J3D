#version 150

in vec3 vsInPosition;
in vec3 vsInColor;

out vec3 fsInColor;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
    gl_Position = uProjection * uView * uModel * vec4(vsInPosition, 1.0);
    fsInColor = vsInColor;
}