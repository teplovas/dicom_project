uniform mat4 transformMatrix;

attribute vec4 in_Position;

//varying vec4 pos;

void main() {

	gl_Position = ftransform();// //Transform the vertex position
	//gl_Position = in_Position;
	// Override gl_Position with our new calculated position
	gl_Position = transformMatrix * gl_Position;// * in_Position;
	//pos = vec4(0.81, 0.53, 0.1, 0.7);//projectionMatrix * viewMatrix * modelMatrix * vec4(50.1, 100.1, 178.1, 89.1);
    gl_TexCoord[0] = gl_MultiTexCoord0;
}
