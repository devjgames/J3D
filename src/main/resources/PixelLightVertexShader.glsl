#version 150

in vec3 vsInPosition;
in vec2 vsInTextureCoordinate;
in vec3 vsInNormal;

out vec3 fsInPosition;
out vec2 fsInTextureCoordinate;
out vec3 fsInNormal;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;
uniform mat4 uModelIT;

void main() {
    gl_Position = uProjection * uView * uModel * vec4(vsInPosition, 1.0);
    
    fsInPosition = (uModel * vec4(vsInPosition, 1.0)).xyz;
    fsInTextureCoordinate =  vsInTextureCoordinate;
    fsInNormal = normalize((uModelIT * vec4(vsInNormal, 0.)).xyz);
}