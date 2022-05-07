package club.mindtech.mindbot.helpers

fun sortToRanks(ranks: List<Pair<String, Int>>): Map<String, Int> {
    var lastVotes = -1
    var lastRank = 0 // Invaid-rank
    val tempMap = mutableMapOf<String, Int>()

    ranks.forEach { (name, votes) ->
        if (votes != lastVotes) {
            lastRank++
        }
        tempMap[name] = lastRank
        lastVotes = votes
    }

    return tempMap.toMap()
}
