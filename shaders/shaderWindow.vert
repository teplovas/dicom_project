//uniform float Angle;

void main() {
//mat4 RotationMatrix = mat4( cos( Angle ), -sin( Angle ), 0.0, 0.0,
	//		    sin( Angle ),  cos( Angle ), 0.0, 0.0,
		//	             0.0,           0.0, 1.0, 0.0,
			//	     0.0,           0.0, 0.0, 1.0 );

	gl_Position = ftransform();// //Transform the vertex position
    gl_TexCoord[0] = gl_MultiTexCoord0;
    //gl_TexCoord[ 0 ] = gl_MultiTexCoord0*RotationMatrix;
}
