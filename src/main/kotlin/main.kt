fun main() {
    fun agoToText(time: Int): String {
        val timeMin = time / 60
        return when (time) {
            in 0..60 -> "был(а) только что"

            //минуты
            in 61..60 * 60 -> {
                val timeToMinute = time / 60
                val lastCharacter = timeToMinute % 10

                when (lastCharacter) {
                    1 -> {
                        if (lastCharacter == 1 && timeToMinute != 11) "был(а) $timeToMinute минуту назад" else "был(а) $timeToMinute минут назад"
                    }
                    2, 3, 4 -> "был(а) $timeToMinute минуты назад"
                    else -> "был(а) $timeToMinute минут назад"
                }
            }

            //часы
            in 60 * 60 + 1..60 * 60 * 24 -> {
                val timeTo = time / (60 * 60)
                val lastCharacter = timeTo % 10

                when (lastCharacter) {
                    1 -> {
                        if (lastCharacter == 1 && timeTo != 11) "был(а) $timeTo час назад" else "был(а) $timeTo часов назад"
                    }
                    2, 3, 4 -> "был(а) $timeTo часа назад"
                    else -> "был(а) $timeTo часов назад"
                }
            }
            //сутки
            in 60 * 60 * 24 + 1..60 * 60 * 24 * 2 -> "был(а) вчера"
            in 60 * 60 * 24 + 1..60 * 60 * 24 * 2 -> "был(а) позавчера"
            else -> "был(а) давно"
        }
    }
    println(agoToText(60 * 60 * 11))
}