package dev.apollointhehouse

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.core.world.ProgressListener

class SaveProgess(
    val onFinish: suspend () -> Unit,
) : ProgressListener {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun progressStart(string: String) {}

    override fun progressStop() {
        scope.launch {
            onFinish()
        }
    }

    override fun progressStage(string: String) {}

    override fun progressStagePercentage(i: Int) {}
}
