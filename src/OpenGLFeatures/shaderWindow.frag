uniform isampler2D texture1;
uniform sampler1D texture2;
uniform int from;
uniform int to;
uniform int width;
uniform int height;
uniform int isUsePalette;
uniform int isByte;

in vec2 pass_TextureCoord;

void main() {
	 vec4 texColor;
	 	int col = texelFetch(texture1,ivec2(gl_TexCoord[0].s*width,gl_TexCoord[0].t*height), 0);//texture2D(texture1, gl_TexCoord[0].st).r;//
	 	if(isByte == 1)
	 	{
	 		col = col << 8;
	 	}
	 	float resCol;
	 	if(col < from)
	 	{
	 		resCol = 0.0;
	 	}
	 	else if(col > to)
	 	{
	 		resCol = 1.0;
	 	}
	 	else
	 	{
	 		resCol = float(col - from)/float(to - from);
	 	}
	 	vec4 tmp;
	 	if(isUsePalette == 1)
	 	{
	 		tmp = texelFetch(texture2, int(resCol * 255.0), 0);//texture1D(texture2, 0.5);
	 	}
	 	else
	 	{
	 		tmp = resCol;
	 	}
	 	texColor.r = tmp.r;//
	 	texColor.g = tmp.g;//resCol;//255.0;
	 	texColor.b = tmp.b;//resCol;//255.0;
	 gl_FragColor = texColor;
}