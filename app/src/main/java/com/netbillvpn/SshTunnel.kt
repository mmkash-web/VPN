package com.netbillvpn

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import java.io.IOException

class SshTunnel(private val host: String, private val port: Int, private val username: String, private val password: String) {

    private var sshClient: SSHClient? = null
    private var session: Session? = null

    @Throws(IOException::class)
    fun connect() {
        sshClient = SSHClient()
        sshClient?.addHostKeyVerifier { _, _, _ -> true } // Accept all keys (for demo only, not secure)
        sshClient?.connect(host, port)
        sshClient?.authPassword(username, password)
        session = sshClient?.startSession()
    }

    @Throws(IOException::class)
    fun disconnect() {
        session?.close()
        sshClient?.disconnect()
    }

    fun isConnected(): Boolean {
        return sshClient?.isConnected == true
    }
}
