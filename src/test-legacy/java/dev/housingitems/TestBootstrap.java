package dev.housingitems;

final class TestBootstrap {
    static net.minecraft.core.HolderLookup.Provider registries() { return net.minecraft.data.registries.VanillaRegistries.createLookup(); }
    static void initialize() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }
}
