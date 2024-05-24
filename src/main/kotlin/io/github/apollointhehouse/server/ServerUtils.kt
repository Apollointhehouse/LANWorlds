package io.github.apollointhehouse.server

import com.b100.utils.FileUtils
import com.mojang.nbt.CompoundTag
import com.mojang.nbt.NbtIo
import io.github.apollointhehouse.LANWorlds.LOGGER
import io.github.apollointhehouse.utils.Result
import net.minecraft.client.Minecraft
import net.minecraft.core.entity.player.EntityPlayer
import net.minecraft.core.world.World
import net.minecraft.core.world.save.LevelData
import java.io.File
import java.net.URL
import java.util.*

object ServerUtils {
	private val mc = Minecraft.getMinecraft(this)
	private val btaVersion = mc.minecraftVersion
	private val SAVES_PATH = "${mc.minecraftDir.path}/saves"
	val serverJarURL = URL("https://github.com/Better-than-Adventure/bta-download-repo/releases/download/v$btaVersion/bta-$btaVersion-server.jar")
	val SERVERS_PATH = "${mc.minecraftDir.path}/servers"

	fun createDirectory(path: String): Result<File> {
		val dir = File(path)
		runCatching {
			if (!dir.exists()) dir.mkdirs()
		}.onFailure {
			return Result.Error("Failed to create directory: $path!")
		}
		return Result.Success(dir)
	}

	fun URL.downloadFile(savePath: String): Result<File> {
		val localFile = File(savePath)
		runCatching {
			if (!localFile.exists()) localFile.createNewFile()
			localFile.writeBytes(readBytes())
		}.onFailure {
			return Result.Error("Failed to download file: $this!")
		}
		return Result.Success(localFile)
	}

	fun createServerProperties(levelData: LevelData, world: World): Result<Properties> {
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
			?: return Result.Error("Failed to create server properties!")

		props.setProperty("default-gamemode", gamemode)
		props.setProperty("level-seed", levelData.randomSeed.toString())
		props.setProperty("world-type", worldType)
		props.setProperty("level-name", worldName)
		props.setProperty("motd", worldName)
		props.setProperty("difficulty", world.difficultySetting.toString())
		props.setProperty("online-mode", "false")

		return Result.Success(props)
	}

	fun Properties.saveTo(path: String): Result<File> {
		val file = File(path)
		runCatching {
			if (!file.exists()) file.createNewFile()
			this.store(file.outputStream(), "")
		}.onFailure {
			return Result.Error("Failed to save server properties file: $path!")
		}
		return Result.Success(file)
	}

	fun World.saveTo(saveLocation: String): Result<File> {
		mc.changeWorld(null)
		val worldName = this.levelData.worldName
		when (createDirectory("$saveLocation/$worldName")) {
			is Result.Error -> return Result.Error("Failed to save world to: $saveLocation!")
			is Result.Success -> {}
		}
		val worldFolder = File("$SAVES_PATH/$worldName")
		runCatching {
			FileUtils.copyAll(worldFolder, File(saveLocation))
		}.onFailure {
			return Result.Error("Failed to save world to: $saveLocation!")
		}
		return Result.Success(worldFolder)
	}

	fun EntityPlayer.saveTo(path: String): Result<File> {
		val datFile = File(path)
		val playerData = CompoundTag()
		runCatching {
			this.saveWithoutId(playerData)
			datFile.parentFile.mkdirs()
			if (!datFile.exists()) datFile.createNewFile()
			NbtIo.writeCompressed(playerData, datFile.outputStream())
		}.onFailure {
			return Result.Error("Failed to save player data!")
		}
		return Result.Success(datFile)
	}

	fun createFile(path: String): Result<File> {
		val file = File(path)
		runCatching {
		    if (!file.exists()) file.createNewFile()
		}.onFailure {
			return Result.Error("Failed to create file: $path!")
		}
		return Result.Success(file)
	}

	fun removeServer(name: String) {
		File("$SERVERS_PATH$name").apply {
			if (!exists()) {
				LOGGER.error("Server directory does not exist!")
				return
			}

			if(!deleteRecursively()) {
				LOGGER.error("Failed to remove server directory!")
				return
			}
		}

		LOGGER.info("Removed server!")
	}
}
