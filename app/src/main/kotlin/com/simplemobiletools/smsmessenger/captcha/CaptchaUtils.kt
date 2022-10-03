package com.simplemobiletools.smsmessenger.captcha

/**
 * Created by Rikka on 2015/12/14.
 */
object CaptchaUtils {
    fun getCaptchaInfo(content: String, number: String?): CaptchaInfo? {
        var content = content
        if (content.isEmpty()) return null

        // 去掉 URL
        val pattern = "[a-zA-z]+://[^\\s]*"
        content = content.replace(pattern.toRegex(), "")

        // 找到并去掉发送者
        val sender = ArrayList<String>()
        content = cutSenderFromString(sender, content, "[", "]")
        content = cutSenderFromString(sender, content, "【", "】")

        // 大概是最短的那个
        sender.sortWith(Comparator { o1, o2 ->
            if (o1.length > o2.length) {
                1
            } else if (o1.length < o2.length) {
                -1
            } else {
                0
            }
        })
        for (oneSender in sender) {
            content = content.replace(oneSender, "")
        }


        // 分出每个句子 并在有关键词的句子里找验证码
        val sentence: ArrayList<String> = findSentence(content)
        for (str in sentence) {
            if (findKeyWord(str) != -1) {
                val code = findCode(str)
                if (code != null) {
                    val captchaInfo = CaptchaInfo()
                    captchaInfo.captcha = code
                    captchaInfo.provider = sender[0]
                    return captchaInfo
                }
            }
        }
        return null
    }

    private fun cutSenderFromString(
        list: ArrayList<String>,
        content: String,
        start: String,
        end: String
    ): String {
        val stringBuilder = StringBuilder(content)
        var curPos = 0
        while (curPos != -1) {
            curPos = content.indexOf(start, curPos)
            if (curPos != -1) {
                curPos++
                val endPos = content.indexOf(end, curPos)
                if (endPos != -1) {
                    list.add(content.substring(curPos, endPos))
                    for (i in curPos until endPos) stringBuilder.setCharAt(i, '-')
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun findKeyWord(content: String): Int {
        var codeFindStart = content.indexOf("验证码")
        if (codeFindStart == -1) codeFindStart = content.indexOf("校验码")
        if (codeFindStart == -1) codeFindStart = content.indexOf("码")
        if (codeFindStart == -1) codeFindStart = content.indexOf("碼")
        if (codeFindStart == -1) codeFindStart = content.indexOf(" code")
        return codeFindStart
    }

    private fun findSentence(content: String): ArrayList<String> {
        val list = ArrayList<String>()
        var last = 0
        for (i in 0 until content.length - 1) {
            val ch = content[i]
            if (ch == ',' || ch == '，' || ch == '.' || ch == '。' || ch == '!' || ch == '！') {
                list.add(content.substring(last, i))
                last = i + 1
            }
        }
        if (last < content.length) {
            list.add(content.substring(last, content.length))
        }
        return list
    }

    private fun isCodeChar(ch: Char): Boolean {
        return ch in 'A'..'Z' || ch in '0'..'9'
    }

    private fun getStringAfter(content: String, string: String): String {
        val startPos = content.indexOf(string)
        return if (startPos == -1) {
            content
        } else {
            content.substring(startPos)
        }
    }

    private fun findCode(content: String): String? {
        var content = content
        content += " "

        // 如果有冒号
        content = getStringAfter(content, "：")
        content = getStringAfter(content, ":")
        var startPos = -1
        for (i in content.indices) {
            val ch = content[i]
            if (startPos == -1 && isCodeChar(ch)) {
                startPos = i
            }
            if (startPos != -1 && !isCodeChar(ch)) {
                // 长度4以上
                if (i - startPos >= 4) {
                    return content.substring(startPos, i)
                }
                startPos = -1
            }
        }
        return null
    }

    class SMSInfo(var sender: String, var code: String)
}
