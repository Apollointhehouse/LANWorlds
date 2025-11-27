package io.github.apollointhehouse.server

import com.b100.utils.FileUtils
import com.mojang.nbt.NbtIo
import com.mojang.nbt.tags.CompoundTag
import net.minecraft.client.Minecraft
import net.minecraft.core.entity.player.Player
import net.minecraft.core.world.World
import net.minecraft.core.world.save.LevelData
import java.io.File
import java.net.URL
import java.util.*

object ServerUtils {
	private val mc = Minecraft.getMinecraft()
	private val btaVersion = mc.minecraftVersion
	private val SAVES_PATH = "${mc.minecraftDir.path}/saves"
	val serverJarURL = URL("https://downloads.betterthanadventure.net/bta-server/release/v${btaVersion}/server.jar")
	val SERVERS_PATH = "${mc.minecraftDir.path}/servers"

	fun createDirectory(path: String): File {
		val dir = File(path)
        if (!dir.exists()) dir.mkdirs()

		return dir
	}

	fun URL.downloadFile(savePath: String): File {
		val localFile = File(savePath)
		runCatching {
			if (!localFile.exists()) localFile.createNewFile()
			localFile.writeBytes(readBytes())
		}.onFailure {
			error("Failed to download file: $this!")
		}

		return localFile
	}

	fun createServerProperties(levelData: LevelData, world: World): Properties {
		val worldName = levelData.worldName
		val gamemode = when (levelData.gamemode) {
			0 -> "Survival"
			1 -> "Creative"
			2 -> "Adventure"
			3 -> "Spectator"
			else -> error("Invalid gamemode!")
		}
		val worldType = "minecraft:" + world.worldType.languageKey.substringAfter('.')

		val props = ClassLoader.getSystemResourceAsStream("server.properties")
			?.let { Properties().apply { load(it) } }
			?: error("Failed to create server properties!")

		props.setProperty("default-gamemode", gamemode)
		props.setProperty("level-seed", levelData.randomSeed.toString())
		props.setProperty("world-type", worldType)
		props.setProperty("level-name", worldName)
		props.setProperty("motd", worldName)
		props.setProperty("difficulty", world.difficulty.toString())
		props.setProperty("online-mode", "false")

		return props
	}

	fun Properties.saveTo(path: String): File {
		val file = File(path)
		runCatching {
			if (!file.exists()) file.createNewFile()
			this.store(file.outputStream(), "")
		}.onFailure {
			error("Failed to save server properties file: $path!")
		}
		return file
	}

	fun LevelData.saveTo(saveLocation: String): File {
		val worldFolder = File("$SAVES_PATH/$worldName")
		val serverWorldFolder = createDirectory("$saveLocation/$worldName")

		mc.changeWorld(null)
		runCatching {
			FileUtils.copyAll(worldFolder, serverWorldFolder)
		}.onFailure {
			error("Failed to save world to: $saveLocation!")
		}
		return serverWorldFolder
	}

	fun Player.saveTo(path: String): File {
		val datFile = File(path)
		val playerData = CompoundTag()
		runCatching {
			saveWithoutId(playerData)
			datFile.parentFile.mkdirs()
			if (!datFile.exists()) datFile.createNewFile()
			NbtIo.writeCompressed(playerData, datFile.outputStream())
		}.onFailure {
			error("Failed to save player data!")
		}

		return datFile
	}

	fun createFile(path: String): File {
		val file = File(path)
		runCatching {
		    if (!file.exists()) file.createNewFile()
		}.onFailure {
			error("Failed to create file: $path!")
		}
		return file
	}
}
