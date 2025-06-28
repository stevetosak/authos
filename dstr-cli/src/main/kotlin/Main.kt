package com.tosak.authos.duster

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands


suspend fun main(args: Array<String>) = DusterCli().subcommands(PullApp(), ViewApp()).main(args)