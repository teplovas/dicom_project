uniform sampler2D texture1;
uniform float palette[768];

void main() {
	 vec4 texColor = texture2D(texture1, gl_TexCoord[0].st);
	 vec3 newColor;
	 int row = clamp(int(texColor.x * 255.0), 0, 255); 
	 //row = 178;
 	 newColor.x = palette[3 * 178]/255.0;
	 newColor.y = palette[3 * row + 1]/255.0;
	 newColor.z = palette[3 * row + 2]/255.0;	
	 
	 gl_FragColor = vec4(newColor,1);
}
/*uniform sampler2D texture1;
uniform sampler1D texture2;
//uniform float palette[768];

void main() {
	 vec4 texColor = texture2D(texture1, gl_TexCoord[0].st);
	 vec3 newColor;
	 int row = int(texColor.x * 255.0);
	 if(row > 225)
	 {
	 	row = 225;
	 }
	 else if(row < 0)
	 {	
	 	row = 0;
	 }
	 
	 if(row == 0)
	 {
	 	 newColor.x = palette[3 * 0]/255.0;
		 newColor.y = palette[3 * 0 + 1]/255.0;
		 newColor.z = palette[3 * 0 + 2]/255.0;	
	 }
	 else if(row == 1)
	 {
	 	 newColor.x = palette[3 * 1]/255.0;
		 newColor.y = palette[3 * 1 + 1]/255.0;
		 newColor.z = palette[3 * 1 + 2]/255.0;	
	 }
	 else if(row == 2)
	 {
	 	 newColor.x = palette[3 * 2]/255.0;
		 newColor.y = palette[3 * 2 + 1]/255.0;
		 newColor.z = palette[3 * 2 + 2]/255.0;	
	 }
	 else if(row == 3)
	 {
	 	 newColor.x = palette[3 * 3]/255.0;
		 newColor.y = palette[3 * 3 + 1]/255.0;
		 newColor.z = palette[3 * 3 + 2]/255.0;	
	 }
	 else if(row == 4)
	 {
	 	 newColor.x = palette[3 * 4]/255.0;
		 newColor.y = palette[3 * 4 + 1]/255.0;
		 newColor.z = palette[3 * 4 + 2]/255.0;	
	 }
	 else if(row == 5)
	 {
	 	 newColor.x = palette[3 * 5]/255.0;
		 newColor.y = palette[3 * 5 + 1]/255.0;
		 newColor.z = palette[3 * 5 + 2]/255.0;	
	 }
	 else if(row == 255)
	 {
	 	 newColor.x = palette[3 * 255]/255.0;
		 newColor.y = palette[3 * 255 + 1]/255.0;
		 newColor.z = palette[3 * 255 + 2]/255.0;	
	 }
	 else if(row == 178)
	 {
	 	 newColor.x = palette[3 * 178]/255.0;
		 newColor.y = palette[3 * 178 + 1]/255.0;
		 newColor.z = palette[3 * 178 + 2]/255.0;	
	 }
	 else if(row == 100)
	 {
	 	 newColor.x = palette[3 * 100]/255.0;
		 newColor.y = palette[3 * 100 + 1]/255.0;
		 newColor.z = palette[3 * 100 + 2]/255.0;	
	 }
	 
	 //newColor.x = float(texture1D(texture2, 667.0).r)/255.0;
	 //newColor.y = float(texture1D(texture2, 667.0).r)/255.0;
	 //newColor.z = float(texture1D(texture2, 667.0).r)/255.0;
	 
		
	 gl_FragColor = vec4(newColor,1);
}*/
