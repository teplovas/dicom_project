uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

attribute vec4 in_Position;

//varying vec4 pos;

void main() {

	gl_Position = ftransform();// //Transform the vertex position
	//gl_Position = in_Position;
	// Override gl_Position with our new calculated position
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * gl_Position;// * in_Position;
	gl_FrontColor = gl_Color;
}
