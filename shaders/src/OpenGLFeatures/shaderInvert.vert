

void main() {
	gl_Position = ftransform(); //Transform the vertex position
    gl_TexCoord[0] = gl_MultiTexCoord0;
}