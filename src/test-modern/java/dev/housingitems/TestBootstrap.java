package dev.housingitems;

/** 26.2+ binds default item components after static registry bootstrap. */
final class TestBootstrap {
    static net.minecraft.core.HolderLookup.Provider registries() { return net.minecraft.data.registries.VanillaRegistries.createLookup(); }
    private static boolean initialized;
    static synchronized void initialize() {
        if (initialized) return;
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        var registries = registries();
        net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registries)
            .forEach(net.minecraft.core.component.DataComponentInitializers.PendingComponents::apply);
        initialized = true;
    }
}
