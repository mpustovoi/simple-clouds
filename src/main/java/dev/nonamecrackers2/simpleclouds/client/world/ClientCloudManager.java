package dev.nonamecrackers2.simpleclouds.client.world;

import dev.nonamecrackers2.simpleclouds.client.cloud.ClientSideCloudTypeManager;
import dev.nonamecrackers2.simpleclouds.client.renderer.SimpleCloudsRenderer;
import dev.nonamecrackers2.simpleclouds.common.cloud.CloudMode;
import dev.nonamecrackers2.simpleclouds.common.cloud.CloudType;
import dev.nonamecrackers2.simpleclouds.common.cloud.SimpleCloudsConstants;
import dev.nonamecrackers2.simpleclouds.common.config.SimpleCloudsConfig;
import dev.nonamecrackers2.simpleclouds.common.world.CloudManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public class ClientCloudManager extends CloudManager<ClientLevel>
{
	private boolean receivedSync;
	
	public ClientCloudManager(ClientLevel level)
	{
		super(level, ClientSideCloudTypeManager.getInstance());
	}
	
	@Override
	public CloudMode getCloudMode()
	{
		if (this.receivedSync && SimpleCloudsConfig.SERVER_SPEC.isLoaded())
			return SimpleCloudsConfig.SERVER.cloudMode.get();
		else
			return SimpleCloudsConfig.CLIENT.cloudMode.get();
	}
	
	@Override
	public String getSingleModeCloudTypeRawId()
	{
		if (this.receivedSync && SimpleCloudsConfig.SERVER_SPEC.isLoaded())
			return SimpleCloudsConfig.SERVER.singleModeCloudType.get();
		else
			return SimpleCloudsConfig.CLIENT.singleModeCloudType.get();
	}
	
	@Override
	protected void resetVanillaWeather()
	{
		this.level.setRainLevel(0.0F);
		this.level.setThunderLevel(0.0F);
	}
	
	@Override
	protected void tickLightning()
	{
		if (this.receivedSync)
			return;
		
		super.tickLightning();
	}
	
	@Override
	protected void attemptToSpawnLightning()
	{
		Minecraft mc = Minecraft.getInstance();
		Camera camera = mc.gameRenderer.getMainCamera();
		int camX = camera.getBlockPosition().getX();
		int camZ = camera.getBlockPosition().getZ();
		for (int i = 0; i < SimpleCloudsConstants.LIGHTNING_SPAWN_ATTEMPTS; i++)
		{
			int x = this.random.nextInt(SimpleCloudsConstants.LIGHTNING_SPAWN_DIAMETER) - SimpleCloudsConstants.LIGHTNING_SPAWN_DIAMETER / 2 + camX;
			int z = this.random.nextInt(SimpleCloudsConstants.LIGHTNING_SPAWN_DIAMETER) - SimpleCloudsConstants.LIGHTNING_SPAWN_DIAMETER / 2 + camZ;
			var info = this.getCloudTypeAtPosition((float)x + 0.5F, (float)z + 0.5F);
			float fade = info.getRight();
			CloudType type = info.getLeft();
			if (!isValidLightning(type, fade, this.random))
				continue;
			this.spawnLightning(type, fade, x, z, this.random.nextInt(3) == 0);
			break;
		}
	}
	
	@Override
	protected void spawnLightning(CloudType type, float fade, int x, int z, boolean soundOnly)
	{
		int y = (int)(type.stormStart() * SimpleCloudsConstants.CLOUD_SCALE + 256.0F);
		float spreadnessFactor = this.random.nextFloat();
		float length = spreadnessFactor * 300.0F + 200.0F;
		float minPitch = 20.0F + spreadnessFactor * 40.0F;
		float maxPitch = 80.0F + spreadnessFactor * 10.0F;
		SimpleCloudsRenderer.getInstance().getWorldEffectsManager().spawnLightning(new BlockPos(x, y, z), soundOnly, this.random.nextInt(), 4, 2, length, 20.0F, minPitch, maxPitch);
	}
	
	@Override
	protected boolean determineUseVanillaWeather()
	{
		return !this.receivedSync || super.determineUseVanillaWeather();
	}
	
	@Override
	public float getSpeed()
	{
		return this.receivedSync ? super.getSpeed() : SimpleCloudsConfig.CLIENT.speedModifier.get().floatValue();
	}
	
	@Override
	public int getCloudHeight()
	{
		return this.receivedSync ? super.getCloudHeight() : SimpleCloudsConfig.CLIENT.cloudHeight.get();
	}
	
	public void setReceivedSync()
	{
		this.receivedSync = true;
	}
	
	public boolean hasReceivedSync()
	{
		return this.receivedSync;
	}
	
	public static boolean isAvailableServerSide()
	{
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null)
			return ((ClientCloudManager)CloudManager.get(mc.level)).hasReceivedSync();
		else
			return false;
	}
}
