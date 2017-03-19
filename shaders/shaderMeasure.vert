uniform mat4 transformMatrix;

attribute vec4 in_Position;

//varying vec4 pos;

void main() {

	gl_Position = ftransform();// //Transform the vertex position
	//gl_Position = in_Position;
	// Override gl_Position with our new calculated position
	gl_Position = transformMatrix * gl_Position;// * in_Position;
	gl_FrontColor = gl_Color;
}
