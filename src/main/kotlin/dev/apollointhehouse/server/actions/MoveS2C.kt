package dev.apollointhehouse.server.actions

class MoveS2C(val proc: Process) : Action {
    override fun run() {
        if (proc.isAlive) return
    }
}
