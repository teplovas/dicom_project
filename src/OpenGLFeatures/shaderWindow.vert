uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

in vec4 in_Position;
in vec2 in_TextureCoord;

out vec2 pass_TextureCoord;

void main(void) {
	gl_Position = in_Position;
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * in_Position;
	//gl_TexCoord[0] = gl_MultiTexCoord0;
}