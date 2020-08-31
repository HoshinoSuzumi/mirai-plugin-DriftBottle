import com.vdurmont.semver4j.Semver
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.description.PluginKind
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.plugin.jvm.JvmMemoryPluginDescription
import net.mamoe.mirai.console.pure.MiraiConsolePureLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import me.redneno.DriftBottle.PluginMain

@ConsoleExperimentalAPI
fun main() {
    MiraiConsolePureLoader.main(arrayOf())

    // 如下启动方案预计在 1.0-RC 支持

    val description = JvmMemoryPluginDescription(
            kind = PluginKind.NORMAL,
            name = "ExamplePlugin",
            author = "Author",
            version = Semver("1.0.0"), // for test
            info = "An example plugin",
            dependencies = listOf(),
            instance = PluginMain
    )
    JarPluginLoader.load(description) // 模拟 "plugin.yml" 加载插件

    PluginMain.enable() // 主动启用插件
}