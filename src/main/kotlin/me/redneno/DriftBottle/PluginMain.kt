package me.redneno.DriftBottle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.redneno.DriftBottle.BottleCommand.throwBottle
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.selectMessages
import net.mamoe.mirai.event.subscribe
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.whileSelectMessages
import net.mamoe.mirai.getFriendOrNull
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent

@Serializable
data class SeaData(
    val sea: MutableList<Bottle>
)

@Serializable
data class Bottle(
    val sender: Long,
    val content: String,
    val isAnonymous: Boolean
)

var World = Json.decodeFromString(SeaData.serializer(), PluginData.seaJson)

object PluginMain : KotlinPlugin() {

    @ConsoleExperimentalAPI
    @ExperimentalPluginConfig
    override fun onEnable() {
        Setting.reload()
        PluginData.reload()

        BottleCommand.register()

        logger.info("德莉芙特已初始化")
    }

    override fun onDisable() {
        BottleCommand.unregister()
    }
}

object PluginData : AutoSavePluginData() {
    //    var bottles: MutableMap<Long?, MutableList<String>> by value()
    var seaJson: String by value("")
}

@ExperimentalPluginConfig
@ConsoleExperimentalAPI
object Setting : AutoSavePluginConfig() {
    var count by value(0)
}

@OptIn(ConsoleExperimentalAPI::class)
object BottleCommand : CompositeCommand(
    PluginMain, "漂流瓶", "db", "plp",
    description = "德莉芙特的漂流瓶", permission = CommandPermission.Any,
    prefixOptional = true
) {
    @SubCommand("扔")
    suspend fun CommandSender.throwBottle(content: String) {
        if (this is MemberCommandSender) {
            val duplicateBottlesFromCommander: List<Bottle> = World.sea.filter {
                it.sender == this.user.id && it.content == content
            }
            PluginMain.logger.warning(duplicateBottlesFromCommander.toString())
            if (duplicateBottlesFromCommander.isEmpty()) {
                newBottle(this.user.id, content, false)
                sendMessage(PluginData.seaJson)
            } else {
                sendMessage("你已经发送过内容相同的漂流瓶了")
            }
        }
    }

    @SubCommand("匿名扔")
    suspend fun CommandSender.throwAnonymousBottle(content: String) {
        if (this is MemberCommandSender) {
            val duplicateBottlesFromCommander: List<Bottle> = World.sea.filter {
                it.sender == this.user.id && it.content == content
            }
            PluginMain.logger.warning(duplicateBottlesFromCommander.toString())
            if (duplicateBottlesFromCommander.isEmpty()) {
                newBottle(this.user.id, content, true)
                sendMessage(PluginData.seaJson)
            } else {
                sendMessage("你已经发送过内容相同的漂流瓶了")
            }
        }
//        if (this is MemberCommandSender) {
//            newBottle(this.user.id, content, true)
//            sendMessage(PluginData.seaJson)
//        }
    }

    @SubCommand("捞")
    suspend fun CommandSender.drag() {
        val randomBottle: Bottle = World.sea[(0 until World.sea.size).random()]
        sendMessage(
            "打捞到一个瓶子~\n里面的纸条上写着\n\n『${randomBottle.content}』\n—— ${
                if (randomBottle.isAnonymous) "未署名"
                else "${bot?.getFriendOrNull(randomBottle.sender)?.nick}(${randomBottle.sender})"
            }"
        )
    }
}

object UserPermission : CommandPermission {
    override fun CommandSender.hasPermission(): Boolean {
        // 高自由度的权限判定

        /*
        return if (this is FriendCommandSender) {
            this.user.id == 123456L
        } else false
        */

        return true
    }
}

fun newBottle(
    uid: Long,
    content: String,
    isAnonymous: Boolean = false
) {
    World.sea.add(Bottle(uid, content, isAnonymous))
    PluginData.seaJson = Json.encodeToString(World)
}
