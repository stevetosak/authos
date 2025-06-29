package com.tosak.authos.duster

import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import com.tosak.authos.duster.commands.Apps
import com.tosak.authos.duster.commands.Credentials
import com.tosak.authos.duster.commands.Sync
import com.tosak.authos.duster.commands.SaveCredentials


suspend fun main(args: Array<String>) =
    DusterCli().subcommands(
        Apps().subcommands(Sync()),
        Credentials().subcommands(SaveCredentials())
).main(args)