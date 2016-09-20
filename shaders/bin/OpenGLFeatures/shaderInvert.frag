
uniform sampler2D texture1;


void main() {
	 vec4 texColor = texture2D(texture1, gl_TexCoord[0].st);
	 texColor.rgb = 1.0 - texColor.rgb;
	 gl_FragColor = texColor;
}