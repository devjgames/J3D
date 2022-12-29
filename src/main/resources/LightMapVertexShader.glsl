#version 150

in vec2 vsInCoord;
in vec3 vsInPosition;
in vec3 vsInNormal;

out vec3 fsInPosition;
out vec3 fsInNormal;

uniform mat4 uProjection;

void main() {
    gl_Position = uProjection * vec4(vsInCoord, 0.0, 1.0);

    fsInPosition = vsInPosition;
    fsInNormal = normalize(vsInNormal);
}