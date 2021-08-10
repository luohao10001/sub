package me.leon

import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.leon.domain.Sumurai
import me.leon.support.*
import org.junit.jupiter.api.Test

class NetworkSubTest {
    @Test
    fun subParse() {
        //        val e = "https://raw.fastgit.org/luohao10001/Sub/master/sub/share/tr"
        //        val e = "https://api.flgwls.com/link/UUDvI5RqtU5ZC098?clash=1r"
        val e = "https://raw.fastgit.org/JACKUSR2089/v2ray-subscribed/master/subscribed/2021-6-13"
        runBlocking {
            Parser.parseFromSub(e)
                .map { it to async(DISPATCHER) { it.SERVER.quickConnect(it.serverPort, 1000) } }
                .filter { it.second.await() > -1 }
                .also { println(it.size) }
                .forEach { println(it.first.info() + ":" + it.second) }
        }

        listOf(
            e,
        )
            .forEach {
                kotlin
                    .runCatching {
                        Parser.parseFromSub(it)
                            .also { println(it.size) }
                            .joinToString(
                                //                        "|",
                                "\r\n",
                                transform = Sub::toUri
                            )
                            .also {
                                println("___________")
                                println(it)
                            }
                    }
                    .onFailure { it.printStackTrace() }
            }
    }

    /** 去除推广 */
    @Test
    fun subRemarkModify() {
        //        val e = "https://zyzmzyz.netlify.app/Clash.yml"
        //        val e = "https://www.linbaoz.com/clash/proxies"
        Parser.debug
        listOf(
            //            "https://fu.stgod.com/clash/proxies",
            //            "https://free.mengbai.cf/clash/proxies",
            //            "https://emby.luoml.eu.org/clash/proxies",
            "https://proxy.yugogo.xyz/vmess/sub",
        )
            .forEach {
                kotlin
                    .runCatching {
                        Parser.parseFromSub(it)
                            .joinToString(
                                //                        "|",
                                "\r\n"
                            ) {
                                it.apply { name = name.replace("\\([^)]+\\)".toRegex(), "") }.info()
                            }
                            .also {
                                println("___________")
                                println(it)
                            }
                    }
                    .onFailure { it.printStackTrace() }
            }
    }

    @Test
    fun sub() {
        val l1 = Parser.parseFromSub("https://etproxypool.ga/clash/proxies")
        val l2 = Parser.parseFromSub("https://suo.yt/v9UsfNr")
        val combine = l1 + l2
        val l1Only = combine - l2
        val l2Only = combine - l1
        val share = l1 - l1Only
        println("共享 ${share.size}")
        println("l1 ${l1.size} 独有 ${l1Only.size}")
        println("l2 ${l2.size} 独有 ${l2Only.size}")
    }

    @Test
    fun load() {
        "http://pan-yz.chaoxing.com/download/downloadfile?fleid=607981566887628800&puid=137229880"
            .readFromNet()
            .also { println(it) }
            .split("\r\n|\n".toRegex())
            .forEach { println(it) }
    }

    @Test
    fun parseNet() {
        val key = SimpleDateFormat("yyyyMMdd").format(Date()).repeat(4)
        "https://ghproxy.com/https://raw.githubusercontent.com/webdao/v2ray/master/nodes.txt"
            .readFromNet()
            .b64Decode()
            .foldIndexed(StringBuilder()) { index, acc, c ->
                acc.also { acc.append((c.code xor key[index % key.length].code).toChar()) }
            }
            .also { println(it) }
            .split("\n")
            .also { println(it.joinToString("|")) }
    }

    @Test
    fun parseSumaraiVpn() {
        runBlocking {
            "https://server.svipvpn.com/opconf.json"
                .readFromNet()
                .fromJson<Sumurai>()
                .data
                .items
                .flatMap { it.items }
                .mapNotNull { Parser.parse(it.ovpn.b64Decode()) }
                .map { it to async(DISPATCHER) { it.SERVER.connect(it.serverPort, 2000) } }
                .filter { it.second.await() > -1 }
                .forEach { println(it.first.toUri()) }
        }
    }
}
