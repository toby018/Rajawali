package rajawali.materials.shaders.fragments.specular;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.methods.DiffuseMethod.DiffuseShaderVar;
import rajawali.materials.methods.SpecularMethod.SpecularShaderVar;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.fragments.LightsVertexShaderFragment.LightsShaderVar;
import rajawali.materials.shaders.fragments.texture.ATextureFragmentShaderFragment;
import rajawali.materials.textures.ATexture;
import android.graphics.Color;
import android.opengl.GLES20;


public class PhongFragmentShaderFragment extends ATextureFragmentShaderFragment implements IShaderFragment {
	public final static String SHADER_ID = "PHONG_FRAGMENT";
	
	private RVec3 muSpecularColor;
	private RFloat muShininess;
	
	private float[] mSpecularColor;
	private float mShininess;
	
	private int muSpecularColorHandle;
	private int muShininessHandle;
	
	private List<ALight> mLights;
	
	public PhongFragmentShaderFragment(List<ALight> lights, int specularColor, float shininess) {
		this(lights, specularColor, shininess, null);
	}
	
	public PhongFragmentShaderFragment(List<ALight> lights, int specularColor, float shininess, List<ATexture> textures) {
		super(textures);
		mSpecularColor = new float[] { 1, 1, 1 };
		mSpecularColor[0] = (float)Color.red(specularColor) / 255.f;
		mSpecularColor[1] = (float)Color.green(specularColor) / 255.f;
		mSpecularColor[2] = (float)Color.blue(specularColor) / 255.f;
		mShininess = shininess;
		mLights = lights;
		mTextures = textures;
		initialize();
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}

	@Override
	public void main() {
		RFloat specular = new RFloat("specular");
		specular.assign(0);
		
		for(int i=0; i<mLights.size(); ++i) {
			RFloat attenuation = (RFloat)getGlobal(LightsShaderVar.V_LIGHT_ATTENUATION, i);
			RFloat lightPower = (RFloat)getGlobal(LightsShaderVar.U_LIGHT_POWER, i);
			RFloat nDotL = (RFloat)getGlobal(DiffuseShaderVar.L_NDOTL, i);
			RFloat spec = new RFloat("spec" + i);
			spec.assign(pow(nDotL, muShininess));
			spec.assign(spec.multiply(attenuation).multiply(lightPower));
			specular.assignAdd(spec);
		}
				
		RVec2 textureCoord = (RVec2)getGlobal(DefaultVar.G_TEXTURE_COORD);
		RVec4 color = (RVec4) getGlobal(DefaultVar.G_COLOR);
		
		if(mTextures != null && mTextures.size() > 0)
		{
			RVec4 specMapColor = new RVec4("specMapColor");
			specMapColor.assign(castVec4(0));

			for(int i=0; i<mTextures.size(); i++)
			{
				RVec4 specColor = new RVec4("specColor" + i);
				specColor.assign(texture2D(muTextures[i], textureCoord));
				specColor.assignMultiply(muInfluence[i]);
				specMapColor.assignAdd(specColor);
			}
			color.rgb().assignAdd(specular.multiply(muSpecularColor).multiply(specMapColor.rgb()));
		}		
		else
		{
			color.rgb().assignAdd(specular.multiply(muSpecularColor));
		}
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		
		muSpecularColor = (RVec3) addUniform(SpecularShaderVar.U_SPECULAR_COLOR);
		muShininess = (RFloat) addUniform(SpecularShaderVar.U_SHININESS);
	}
	
	@Override
	public void setLocations(int programHandle) {
		super.setLocations(programHandle);
		muSpecularColorHandle = getUniformLocation(programHandle, SpecularShaderVar.U_SPECULAR_COLOR);
		muShininessHandle = getUniformLocation(programHandle, SpecularShaderVar.U_SHININESS);
	}
	
	@Override
	public void applyParams() {
		super.applyParams();
		GLES20.glUniform3fv(muSpecularColorHandle, 1, mSpecularColor, 0);
		GLES20.glUniform1f(muShininessHandle, mShininess);
	}
	
	public void setSpecularColor(int color)
	{
		mSpecularColor[0] = (float)Color.red(color) / 255.f;
		mSpecularColor[1] = (float)Color.green(color) / 255.f;
		mSpecularColor[2] = (float)Color.blue(color) / 255.f;
	}
	
	public void setShininess(float shininess)
	{
		mShininess = shininess;
	}
}
