package dev.apollointhehouse

import dev.apollointhehouse.server.actions.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.core.world.ProgressListener

class SaveProgess(
    val onFinish: Action,
) : ProgressListener {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun progressStart(string: String) {}

    override fun progressStop() {
        scope.launch {
            onFinish.run()
        }
    }

    override fun progressStage(string: String) {}

    override fun progressStagePercentage(i: Int) {}
}
