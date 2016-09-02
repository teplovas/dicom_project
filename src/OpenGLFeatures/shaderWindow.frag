
uniform sampler2D texture1;
uniform float from;
uniform float to;

void main() {
	 vec4 texColor;
	 	float col = texture2D(texture1, gl_TexCoord[0].st);
	 	texColor.r = col;
	 	texColor.g = col;
	 	texColor.b = col;
	 gl_FragColor = texColor;
}