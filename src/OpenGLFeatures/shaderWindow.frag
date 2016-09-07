#extension GL_EXT_gpu_shader4 : require
#extension GL_EXT_geometry_shader4 : require
//#extension GL_EXT_texture_integer : require
uniform isampler2D texture1;
uniform sampler1D texture2;
uniform int from;
uniform int to;
uniform int min;
uniform int max;

void main() {
	 vec4 texColor;
	 	int col = texelFetch(texture1,ivec2(gl_TexCoord[0].s*512,gl_TexCoord[0].t*512), 0);//texture2D(texture1, gl_TexCoord[0].st).r;//
	 	
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
	 	vec4 tmp = texture1D(texture2, resCol);
	 	texColor.r = tmp.r;//resCol;//
	 	texColor.g = tmp.g;//resCol;//255.0;
	 	texColor.b = tmp.b;//resCol;//255.0;
	 gl_FragColor = texColor;
}