public record Stat(
        long ai1TotalTime,
        long ai2TotalTime,
        long ai1LongestTime,
        long ai2LongestTime,
        int ai1Moves,
        int ai2Moves,
        Result winner
) { }