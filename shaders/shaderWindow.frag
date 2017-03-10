uniform isampler2D texture1;
uniform sampler1D texture2;
uniform int from;
uniform int to;
uniform int width;
uniform int height;
uniform int isUsePalette;
uniform int isInvert;

//varying vec4 pos;

void main() {
	 vec4 texColor;
	 	int col = texelFetch(texture1,ivec2(gl_TexCoord[0].s*width,gl_TexCoord[0].t*height), 0);//texture2D(texture1, gl_TexCoord[0].st).r;//
	 	
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
	 	if(isInvert == 1)
	 	{
	 		resCol = 1 - resCol;
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