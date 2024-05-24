package io.github.apollointhehouse.mixin

import net.minecraft.core.world.World
import net.minecraft.core.world.save.LevelData
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(value = [World::class], remap = false)
interface WorldAccessor {
	@get:Accessor("levelData")
	val levelData: LevelData
}
