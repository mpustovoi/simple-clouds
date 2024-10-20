package dev.nonamecrackers2.simpleclouds.common.cloud.region;

public interface RegionType
{
	RegionType.Result getCloudTypeIndexAt(float x, float z, float scale, int totalCloudTypes);
	
	public static record Result(int index, float fade) {}
}
