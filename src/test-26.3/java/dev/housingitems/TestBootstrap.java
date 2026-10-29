package dev.housingitems;

final class TestBootstrap {
    private static boolean initialized;
    static net.minecraft.core.HolderLookup.Provider registries() { return net.minecraft.data.registries.VanillaRegistries.createWorldLookup(); }
    static synchronized void initialize() {
        if (initialized) return;
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registries())
            .forEach(net.minecraft.core.component.DataComponentInitializers.PendingComponents::apply);
        initialized = true;
    }
}
