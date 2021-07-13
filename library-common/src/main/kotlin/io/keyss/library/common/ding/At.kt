package io.keyss.library.common.ding

/**
 * @author Key
 * Time: 2019/11/26 14:45
 * Description:
 */
data class At(var isAtAll: Boolean = false) {
    var atMobiles: List<String>? = null

    constructor(mobiles: List<String>) : this() {
        atMobiles = mobiles
    }
}