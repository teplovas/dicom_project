
uniform sampler2D texture1;
uniform float from;
uniform float to;

void main() {
	 vec4 texColor = texture2D(texture1, gl_TexCoord[0].st);
	 float rgbFrom = from / 255.0;
	 float rgbTo = to / 255.0;
	 if(texColor.r < rgbFrom)
	 {
	 	texColor.r = rgbFrom;
	 	texColor.g = rgbFrom;
	 	texColor.b = rgbFrom;
	 }
	 else if(texColor.r > rgbTo)
	 {
	 	texColor.r = rgbTo;
	 	texColor.g = rgbTo;
	 	texColor.b = rgbTo;
	 }
	 gl_FragColor = texColor;
}